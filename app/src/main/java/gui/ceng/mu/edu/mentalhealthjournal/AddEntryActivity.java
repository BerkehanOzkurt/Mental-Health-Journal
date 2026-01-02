package gui.ceng.mu.edu.mentalhealthjournal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.fragment.EmotionsFragment;
import gui.ceng.mu.edu.mentalhealthjournal.fragment.SleepFragment;

import java.util.Calendar;
import java.util.List;

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

    private int moodLevel = 3; // Default to normal
    private long entryTimestamp;

    private ImageView selectedMoodIcon;
    private EditText editQuickNote;
    private ImageView photoPreview;

    private EmotionsFragment emotionsFragment;
    private SleepFragment sleepFragment;

    private JournalRepository repository;
    private Handler mainHandler;

    // Photo and voice memo paths (to be implemented)
    private String photoPath = null;
    private String voiceMemoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        // Initialize repository for database operations
        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

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
        btnOpenFullNote.setOnClickListener(v -> {
            // TODO: Open full note editor activity
            Toast.makeText(this, "Full note editor coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Take photo
        MaterialButton btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(v -> {
            // TODO: Implement camera intent
            Toast.makeText(this, "Camera feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // From gallery
        MaterialButton btnFromGallery = findViewById(R.id.btn_from_gallery);
        btnFromGallery.setOnClickListener(v -> {
            // TODO: Implement gallery intent
            Toast.makeText(this, "Gallery feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Record voice memo
        MaterialButton btnRecordVoice = findViewById(R.id.btn_record_voice);
        btnRecordVoice.setOnClickListener(v -> {
            // TODO: Implement voice recording
            Toast.makeText(this, "Voice memo feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Edit activities
        View btnEditActivities = findViewById(R.id.btn_edit_activities);
        btnEditActivities.setOnClickListener(v -> {
            // TODO: Open activity editor
            Toast.makeText(this, "Edit activities coming soon!", Toast.LENGTH_SHORT).show();
        });
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
        // Create new entry
        JournalEntryEntity entry = new JournalEntryEntity();
        entry.setTimestamp(entryTimestamp);
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

        // Save to database in background thread
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't shutdown repository here as it might be used by other activities
    }
}
