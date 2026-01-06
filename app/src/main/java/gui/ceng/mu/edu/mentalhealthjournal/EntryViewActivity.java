package gui.ceng.mu.edu.mentalhealthjournal;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

/**
 * Activity for viewing a journal entry's full details.
 * Shows mood, emotions, sleep tags, notes, photos, and voice memos.
 */
public class EntryViewActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entry_id";

    private JournalRepository repository;
    private Handler mainHandler;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    // Views
    private ImageView moodIcon;
    private TextView moodLabel;
    private TextView dateTimeText;
    private TextView noteText;
    private ChipGroup emotionsChipGroup;
    private ChipGroup sleepChipGroup;
    private LinearLayout emotionsContainer;
    private LinearLayout sleepContainer;
    private LinearLayout noteContainer;
    private LinearLayout photoContainer;
    private LinearLayout voiceContainer;
    private ImageView photoPreview;
    private MaterialButton playVoiceButton;

    private JournalEntryEntity currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_view);

        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        loadEntry();
    }

    private void initViews() {
        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        moodIcon = findViewById(R.id.mood_icon);
        moodLabel = findViewById(R.id.mood_label);
        dateTimeText = findViewById(R.id.date_time_text);
        noteText = findViewById(R.id.note_text);
        emotionsChipGroup = findViewById(R.id.emotions_chip_group);
        sleepChipGroup = findViewById(R.id.sleep_chip_group);
        emotionsContainer = findViewById(R.id.emotions_container);
        sleepContainer = findViewById(R.id.sleep_container);
        noteContainer = findViewById(R.id.note_container);
        photoContainer = findViewById(R.id.photo_container);
        voiceContainer = findViewById(R.id.voice_container);
        photoPreview = findViewById(R.id.photo_preview);
        playVoiceButton = findViewById(R.id.play_voice_button);
    }

    private void loadEntry() {
        long entryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
        if (entryId == -1) {
            Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository.getEntryById(entryId, new JournalRepository.RepositoryCallback<JournalEntryEntity>() {
            @Override
            public void onComplete(JournalEntryEntity entry) {
                mainHandler.post(() -> {
                    if (entry != null) {
                        currentEntry = entry;
                        displayEntry(entry);
                    } else {
                        Toast.makeText(EntryViewActivity.this, "Entry not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(EntryViewActivity.this, "Error loading entry", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayEntry(JournalEntryEntity entry) {
        // Set mood icon and label
        moodIcon.setImageResource(entry.getMoodIconResource());
        moodIcon.setBackgroundResource(entry.getMoodBackgroundResource());
        moodLabel.setText(getMoodLabel(entry.getMoodLevel()));
        moodLabel.setTextColor(getMoodColor(entry.getMoodLevel()));

        // Set date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy • HH:mm", Locale.getDefault());
        dateTimeText.setText(dateFormat.format(new Date(entry.getTimestamp())));

        // Set emotions
        List<String> emotions = entry.getEmotions();
        if (emotions != null && !emotions.isEmpty()) {
            emotionsContainer.setVisibility(View.VISIBLE);
            emotionsChipGroup.removeAllViews();
            for (String emotion : emotions) {
                Chip chip = new Chip(this);
                chip.setText(emotion);
                chip.setChipBackgroundColorResource(R.color.very_good);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chip.setClickable(false);
                emotionsChipGroup.addView(chip);
            }
        } else {
            emotionsContainer.setVisibility(View.GONE);
        }

        // Set sleep tags
        List<String> sleepTags = entry.getSleepTags();
        if (sleepTags != null && !sleepTags.isEmpty()) {
            sleepContainer.setVisibility(View.VISIBLE);
            sleepChipGroup.removeAllViews();
            for (String sleep : sleepTags) {
                Chip chip = new Chip(this);
                chip.setText(sleep);
                chip.setChipBackgroundColorResource(R.color.normal);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chip.setClickable(false);
                sleepChipGroup.addView(chip);
            }
        } else {
            sleepContainer.setVisibility(View.GONE);
        }

        // Set note
        String note = entry.getNote();
        if (note != null && !note.isEmpty()) {
            noteContainer.setVisibility(View.VISIBLE);
            noteText.setText(note);
        } else {
            noteContainer.setVisibility(View.GONE);
        }

        // Set photo
        String photoPath = entry.getPhotoPath();
        if (photoPath != null && new File(photoPath).exists()) {
            photoContainer.setVisibility(View.VISIBLE);
            photoPreview.setImageURI(Uri.fromFile(new File(photoPath)));
            photoPreview.setOnClickListener(v -> viewFullPhoto(photoPath));
        } else {
            photoContainer.setVisibility(View.GONE);
        }

        // Set voice memo
        String voicePath = entry.getVoiceMemoPath();
        if (voicePath != null && new File(voicePath).exists()) {
            voiceContainer.setVisibility(View.VISIBLE);
            playVoiceButton.setOnClickListener(v -> toggleVoicePlayback(voicePath));
        } else {
            voiceContainer.setVisibility(View.GONE);
        }
    }

    private String getMoodLabel(int moodLevel) {
        switch (moodLevel) {
            case 5: return "Feeling Rad!";
            case 4: return "Feeling Good";
            case 3: return "Feeling Meh";
            case 2: return "Feeling Bad";
            case 1: return "Feeling Awful";
            default: return "Mood";
        }
    }

    private int getMoodColor(int moodLevel) {
        switch (moodLevel) {
            case 5: return getResources().getColor(R.color.very_good);
            case 4: return getResources().getColor(R.color.good);
            case 3: return getResources().getColor(R.color.normal);
            case 2: return getResources().getColor(R.color.bad);
            case 1: return getResources().getColor(R.color.very_bad);
            default: return getResources().getColor(android.R.color.white);
        }
    }

    private void viewFullPhoto(String photoPath) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(photoPath));
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "image/*");
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleVoicePlayback(String voicePath) {
        if (isPlaying && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            playVoiceButton.setText("▶ Play Voice Memo");
            playVoiceButton.setIconResource(R.drawable.ic_mic);
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(voicePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            playVoiceButton.setText("⏹ Stop Playing");

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playVoiceButton.setText("▶ Play Voice Memo");
                playVoiceButton.setIconResource(R.drawable.ic_mic);
                mp.release();
                mediaPlayer = null;
            });
        } catch (Exception e) {
            Toast.makeText(this, "Cannot play voice memo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
