package gui.ceng.mu.edu.mentalhealthjournal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.fragment.EmotionsFragment;
import gui.ceng.mu.edu.mentalhealthjournal.fragment.SleepFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity for adding a new journal entry.
 * Contains fragments for selecting emotions, sleep quality, and activities.
 * Demonstrates Fragment usage and background thread for database operations.
 */
public class AddEntryActivity extends AppCompatActivity {

    public static final String EXTRA_MOOD_LEVEL = "mood_level";
    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_YEAR = "year";
    public static final String EXTRA_TIMESTAMP = "timestamp";
    public static final String EXTRA_ENTRY_ID = "entry_id";

    private int moodLevel = 3; // Default to normal
    private long entryTimestamp;
    private long editEntryId = -1; // -1 means new entry, otherwise editing existing
    private JournalEntryEntity existingEntry = null;

    private ImageView selectedMoodIcon;
    private EditText editQuickNote;
    private ImageView photoPreview;
    private MaterialButton btnRecordVoice;
    private View voicePlaybackContainer;
    private TextView voiceDurationText;

    private EmotionsFragment emotionsFragment;
    private SleepFragment sleepFragment;

    private JournalRepository repository;
    private Handler mainHandler;

    // Photo and voice memo paths
    private String photoPath = null;
    private String voiceMemoPath = null;
    private Uri currentPhotoUri = null;

    // Voice recording
    private MediaRecorder mediaRecorder = null;
    private MediaPlayer mediaPlayer = null;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private long recordingStartTime = 0;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> noteEditorLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        // Initialize repository for database operations
        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // Setup activity result launchers
        setupActivityLaunchers();

        // Check if editing existing entry
        editEntryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);

        // Get mood level from intent
        moodLevel = getIntent().getIntExtra(EXTRA_MOOD_LEVEL, 3);
        
        // Get timestamp from intent or use current time
        entryTimestamp = getIntent().getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis());
        
        // If day/month/year are provided, construct the timestamp
        if (getIntent().hasExtra(EXTRA_DAY)) {
            int day = getIntent().getIntExtra(EXTRA_DAY, 1);
            int month = getIntent().getIntExtra(EXTRA_MONTH, 0);
            int year = getIntent().getIntExtra(EXTRA_YEAR, 2025);
            
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            // Keep current time for the timestamp
            Calendar now = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
            entryTimestamp = cal.getTimeInMillis();
        }

        initViews();
        setupFragments();
        setupClickListeners();
        updateMoodIcon();

        // If editing, load the existing entry data
        if (editEntryId != -1) {
            loadExistingEntry();
        }
    }

    private void setupActivityLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && currentPhotoUri != null) {
                    photoPath = currentPhotoUri.getPath();
                    showPhotoPreview(currentPhotoUri);
                }
            }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        photoPath = copyImageToAppStorage(selectedImageUri);
                        if (photoPath != null) {
                            showPhotoPreview(Uri.fromFile(new File(photoPath)));
                        }
                    }
                }
            }
        );

        // Note editor launcher
        noteEditorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String note = result.getData().getStringExtra(NoteEditorActivity.RESULT_NOTE);
                    if (note != null && editQuickNote != null) {
                        editQuickNote.setText(note);
                    }
                }
            }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // Check if all permissions granted
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (!allGranted) {
                    Toast.makeText(this, "Permission required for this feature", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void loadExistingEntry() {
        repository.getEntryById(editEntryId, new JournalRepository.RepositoryCallback<JournalEntryEntity>() {
            @Override
            public void onComplete(JournalEntryEntity entry) {
                mainHandler.post(() -> {
                    if (entry != null) {
                        existingEntry = entry;
                        moodLevel = entry.getMoodLevel();
                        entryTimestamp = entry.getTimestamp();
                        updateMoodIcon();
                        
                        // Set note
                        if (entry.getNote() != null) {
                            editQuickNote.setText(entry.getNote());
                        }
                        
                        // Set photo/voice paths and show previews
                        photoPath = entry.getPhotoPath();
                        voiceMemoPath = entry.getVoiceMemoPath();
                        
                        // Show existing photo
                        if (photoPath != null && new File(photoPath).exists()) {
                            showPhotoPreview(Uri.fromFile(new File(photoPath)));
                        }
                        
                        // Show existing voice memo
                        if (voiceMemoPath != null && new File(voiceMemoPath).exists()) {
                            btnRecordVoice.setText("🎤 Voice Memo - Tap for options");
                        }
                        
                        // Set emotions in fragment after a short delay for fragment to be ready
                        mainHandler.postDelayed(() -> {
                            if (entry.getEmotions() != null) {
                                emotionsFragment.setSelectedEmotions(entry.getEmotions());
                            }
                            if (entry.getSleepTags() != null) {
                                sleepFragment.setSelectedSleepOptions(entry.getSleepTags());
                            }
                        }, 100);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> Toast.makeText(AddEntryActivity.this, 
                    "Failed to load entry", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void initViews() {
        selectedMoodIcon = findViewById(R.id.selected_mood_icon);
        editQuickNote = findViewById(R.id.edit_quick_note);
        photoPreview = findViewById(R.id.photo_preview);
    }

    private void setupFragments() {
        // Create and add Emotions Fragment
        emotionsFragment = EmotionsFragment.newInstance();
        
        // Create and add Sleep Fragment
        sleepFragment = SleepFragment.newInstance();

        // Add fragments to their containers
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_emotions_container, emotionsFragment);
        transaction.replace(R.id.fragment_sleep_container, sleepFragment);
        transaction.commit();
    }

    private void setupClickListeners() {
        // Back button
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Save button (top)
        TextView btnSaveTop = findViewById(R.id.btn_save_top);
        btnSaveTop.setOnClickListener(v -> saveEntry());

        // Save button (FAB)
        FloatingActionButton fabSave = findViewById(R.id.fab_save);
        fabSave.setOnClickListener(v -> saveEntry());

        // Open full note
        TextView btnOpenFullNote = findViewById(R.id.btn_open_full_note);
        btnOpenFullNote.setOnClickListener(v -> openFullNoteEditor());

        // Take photo
        MaterialButton btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(v -> takePhoto());

        // From gallery
        MaterialButton btnFromGallery = findViewById(R.id.btn_from_gallery);
        btnFromGallery.setOnClickListener(v -> pickFromGallery());

        // Record voice memo
        btnRecordVoice = findViewById(R.id.btn_record_voice);
        btnRecordVoice.setOnClickListener(v -> toggleVoiceRecording());

        // Edit activities
        View btnEditActivities = findViewById(R.id.btn_edit_activities);
        btnEditActivities.setOnClickListener(v -> {
            Toast.makeText(this, "Edit activities coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    // ===================== NOTE EDITOR =====================

    /**
     * Opens the full-screen note editor with the current note content.
     */
    private void openFullNoteEditor() {
        String currentNote = "";
        if (editQuickNote != null && editQuickNote.getText() != null) {
            currentNote = editQuickNote.getText().toString();
        }
        
        Intent intent = NoteEditorActivity.createIntent(this, (int) editEntryId, currentNote);
        noteEditorLauncher.launch(intent);
    }

    // ===================== CAMERA FUNCTIONALITY =====================

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JOURNAL_" + timeStamp;
            File storageDir = new File(getExternalFilesDir(null), "Pictures");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            File image = new File(storageDir, imageFileName + ".jpg");
            photoPath = image.getAbsolutePath();
            return image;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void pickFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        galleryLauncher.launch(pickIntent);
    }

    private String copyImageToAppStorage(Uri sourceUri) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = "JOURNAL_" + timeStamp + ".jpg";
            File storageDir = new File(getExternalFilesDir(null), "Pictures");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            File destFile = new File(storageDir, imageFileName);

            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            FileOutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showPhotoPreview(Uri imageUri) {
        try {
            photoPreview.setVisibility(View.VISIBLE);
            photoPreview.setImageURI(imageUri);
            
            // Add click listener to show options
            photoPreview.setOnClickListener(v -> showPhotoOptions());
            
            Toast.makeText(this, "Photo added!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoOptions() {
        new AlertDialog.Builder(this)
            .setTitle("Photo Options")
            .setItems(new String[]{"View Full Size", "Remove Photo"}, (dialog, which) -> {
                if (which == 0) {
                    // View full size - open in gallery app
                    if (photoPath != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(photoPath));
                        intent.setDataAndType(uri, "image/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);
                    }
                } else {
                    // Remove photo
                    photoPath = null;
                    photoPreview.setVisibility(View.GONE);
                    photoPreview.setImageDrawable(null);
                    Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    // ===================== VOICE RECORDING FUNCTIONALITY =====================

    private void toggleVoiceRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO});
            return;
        }

        if (isRecording) {
            stopRecording();
        } else {
            // If there's already a recording, ask before overwriting
            if (voiceMemoPath != null) {
                new AlertDialog.Builder(this)
                    .setTitle("Voice Memo")
                    .setMessage("You already have a voice memo. What would you like to do?")
                    .setPositiveButton("Record New", (d, w) -> startRecording())
                    .setNeutralButton("Play", (d, w) -> playVoiceMemo())
                    .setNegativeButton("Delete", (d, w) -> deleteVoiceMemo())
                    .show();
            } else {
                startRecording();
            }
        }
    }

    private void startRecording() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String audioFileName = "VOICE_" + timeStamp + ".m4a";
            File storageDir = new File(getExternalFilesDir(null), "Audio");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            File audioFile = new File(storageDir, audioFileName);
            voiceMemoPath = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(voiceMemoPath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            btnRecordVoice.setText("⏹ Stop Recording");
            btnRecordVoice.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.very_bad));

            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
            voiceMemoPath = null;
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

                isRecording = false;
                long duration = (System.currentTimeMillis() - recordingStartTime) / 1000;
                btnRecordVoice.setText("🎤 Voice Memo (" + duration + "s) - Tap for options");
                btnRecordVoice.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));

                Toast.makeText(this, "Recording saved! (" + duration + " seconds)", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                voiceMemoPath = null;
            }
        }
    }

    private void playVoiceMemo() {
        if (voiceMemoPath == null) return;

        if (isPlaying && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            btnRecordVoice.setText("🎤 Voice Memo - Tap for options");
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(voiceMemoPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            btnRecordVoice.setText("⏹ Playing...");

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnRecordVoice.setText("🎤 Voice Memo - Tap for options");
                mp.release();
                mediaPlayer = null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to play recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteVoiceMemo() {
        if (voiceMemoPath != null) {
            File file = new File(voiceMemoPath);
            if (file.exists()) {
                file.delete();
            }
            voiceMemoPath = null;
            btnRecordVoice.setText("Tap to Record");
            btnRecordVoice.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.darker_gray));
            Toast.makeText(this, "Voice memo deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateMoodIcon() {
        int iconRes;
        int bgRes;
        switch (moodLevel) {
            case 5:
                iconRes = R.drawable.face1;
                bgRes = R.drawable.emoji_background_very_good;
                break;
            case 4:
                iconRes = R.drawable.face2;
                bgRes = R.drawable.emoji_background_good;
                break;
            case 3:
                iconRes = R.drawable.face3;
                bgRes = R.drawable.emoji_background_normal;
                break;
            case 2:
                iconRes = R.drawable.face4;
                bgRes = R.drawable.emoji_background_bad;
                break;
            case 1:
            default:
                iconRes = R.drawable.face5;
                bgRes = R.drawable.emoji_background_very_bad;
                break;
        }
        selectedMoodIcon.setImageResource(iconRes);
        selectedMoodIcon.setBackgroundResource(bgRes);
    }

    /**
     * Save the journal entry to the database.
     * Uses background thread via JournalRepository.
     */
    private void saveEntry() {
        // Create or update entry
        JournalEntryEntity entry;
        if (existingEntry != null) {
            entry = existingEntry;
        } else {
            entry = new JournalEntryEntity();
            entry.setTimestamp(entryTimestamp);
        }
        entry.setMoodLevel(moodLevel);

        // Get selected emotions from fragment
        List<String> selectedEmotions = emotionsFragment.getSelectedEmotions();
        entry.setEmotions(selectedEmotions);

        // Get selected sleep options from fragment
        List<String> selectedSleep = sleepFragment.getSelectedSleepOptions();
        entry.setSleepTags(selectedSleep);

        // Get quick note
        String note = editQuickNote.getText().toString().trim();
        if (!note.isEmpty()) {
            entry.setNote(note);
        }

        // Set photo and voice memo paths
        entry.setPhotoPath(photoPath);
        entry.setVoiceMemoPath(voiceMemoPath);

        // Save or update in database
        if (existingEntry != null) {
            // Update existing entry
            repository.update(entry, new JournalRepository.RepositoryCallback<Void>() {
                @Override
                public void onComplete(Void result) {
                    mainHandler.post(() -> {
                        Toast.makeText(AddEntryActivity.this, 
                                "Entry updated successfully!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(Exception e) {
                    mainHandler.post(() -> {
                        Toast.makeText(AddEntryActivity.this, 
                                "Failed to update entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // Insert new entry
            repository.insert(entry, new JournalRepository.RepositoryCallback<Long>() {
            @Override
            public void onComplete(Long result) {
                // Run on main thread to show toast and finish
                mainHandler.post(() -> {
                    Toast.makeText(AddEntryActivity.this, 
                            "Entry saved successfully!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(AddEntryActivity.this, 
                            "Failed to save entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release media resources
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop recording if activity is stopped
        if (isRecording) {
            stopRecording();
        }
        // Stop playback
        if (isPlaying && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }
}
