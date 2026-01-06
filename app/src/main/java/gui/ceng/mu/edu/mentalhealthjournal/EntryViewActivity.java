package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.util.List;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.util.DateUtils;
import gui.ceng.mu.edu.mentalhealthjournal.util.MoodUtils;

public class EntryViewActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "entry_id";

    private JournalRepository repository;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;

    private ImageView moodIcon;
    private TextView moodLabel, dateTimeText, noteText;
    private ChipGroup emotionsChipGroup, sleepChipGroup;
    private LinearLayout emotionsContainer, sleepContainer;
    private View noteContainer, photoContainer, voiceContainer;
    private ImageView photoPreview;
    private MaterialButton playVoiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_view);
        repository = new JournalRepository(this);
        initViews();
        loadEntry();
    }

    private void initViews() {
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
        if (entryId == -1) { Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show(); finish(); return; }

        repository.getEntryById(entryId, new JournalRepository.RepositoryCallback<JournalEntryEntity>() {
            @Override public void onComplete(JournalEntryEntity entry) {
                mainHandler.post(() -> { if (entry != null) displayEntry(entry); else { Toast.makeText(EntryViewActivity.this, "Entry not found", Toast.LENGTH_SHORT).show(); finish(); } });
            }
            @Override public void onError(Exception e) {
                mainHandler.post(() -> { Toast.makeText(EntryViewActivity.this, "Error loading entry", Toast.LENGTH_SHORT).show(); finish(); });
            }
        });
    }

    private void displayEntry(JournalEntryEntity entry) {
        moodIcon.setImageResource(entry.getMoodIconResource());
        moodIcon.setBackgroundResource(entry.getMoodBackgroundResource());
        moodLabel.setText(MoodUtils.getText(entry.getMoodLevel()));
        moodLabel.setTextColor(ContextCompat.getColor(this, MoodUtils.getColorRes(entry.getMoodLevel())));
        dateTimeText.setText(DateUtils.format(entry.getTimestamp(), "EEEE, d MMMM yyyy • HH:mm"));

        setupChips(entry.getEmotions(), emotionsContainer, emotionsChipGroup, R.color.very_good);
        setupChips(entry.getSleepTags(), sleepContainer, sleepChipGroup, R.color.normal);

        String note = entry.getNote();
        noteContainer.setVisibility(note != null && !note.isEmpty() ? View.VISIBLE : View.GONE);
        if (note != null) noteText.setText(note);

        String photoPath = entry.getPhotoPath();
        boolean hasPhoto = photoPath != null && new File(photoPath).exists();
        photoContainer.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);
        if (hasPhoto) { photoPreview.setImageURI(Uri.fromFile(new File(photoPath))); photoPreview.setOnClickListener(v -> viewFullPhoto(photoPath)); }

        String voicePath = entry.getVoiceMemoPath();
        boolean hasVoice = voicePath != null && new File(voicePath).exists();
        voiceContainer.setVisibility(hasVoice ? View.VISIBLE : View.GONE);
        if (hasVoice) playVoiceButton.setOnClickListener(v -> toggleVoicePlayback(voicePath));
    }

    private void setupChips(List<String> items, LinearLayout container, ChipGroup group, int colorRes) {
        if (items != null && !items.isEmpty()) {
            container.setVisibility(View.VISIBLE);
            group.removeAllViews();
            for (String item : items) {
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setChipBackgroundColorResource(colorRes);
                chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                chip.setClickable(false);
                group.addView(chip);
            }
        } else container.setVisibility(View.GONE);
    }

    private void viewFullPhoto(String path) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(path));
            Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) { Toast.makeText(this, "Cannot open photo", Toast.LENGTH_SHORT).show(); }
    }

    private void toggleVoicePlayback(String path) {
        if (isPlaying && mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); mediaPlayer = null; isPlaying = false; playVoiceButton.setText("▶ Play Voice Memo"); return; }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            playVoiceButton.setText("⏹ Stop Playing");
            mediaPlayer.setOnCompletionListener(mp -> { isPlaying = false; playVoiceButton.setText("▶ Play Voice Memo"); mp.release(); mediaPlayer = null; });
        } catch (Exception e) { Toast.makeText(this, "Cannot play voice memo", Toast.LENGTH_SHORT).show(); }
    }

    @Override protected void onDestroy() { super.onDestroy(); if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; } }
}
