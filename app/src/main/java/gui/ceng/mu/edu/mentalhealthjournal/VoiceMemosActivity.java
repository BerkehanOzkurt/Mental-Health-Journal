package gui.ceng.mu.edu.mentalhealthjournal;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

/**
 * Activity displaying all voice memos from journal entries.
 */
public class VoiceMemosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VoiceMemoAdapter adapter;
    private List<VoiceMemoItem> voiceMemos;
    private JournalRepository repository;
    private Handler mainHandler;
    private TextView emptyText;
    private MediaPlayer mediaPlayer;
    private int currentlyPlayingPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_memos);

        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        loadVoiceMemos();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        emptyText = findViewById(R.id.empty_text);
        recyclerView = findViewById(R.id.memos_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        voiceMemos = new ArrayList<>();
        adapter = new VoiceMemoAdapter(this, voiceMemos, this::playVoiceMemo);
        recyclerView.setAdapter(adapter);
    }

    private void loadVoiceMemos() {
        repository.getAllEntries().observe(this, entities -> {
            voiceMemos.clear();

            if (entities != null) {
                for (JournalEntryEntity entity : entities) {
                    String voicePath = entity.getVoiceMemoPath();
                    if (voicePath != null && new File(voicePath).exists()) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault());
                        String dateStr = dateFormat.format(new Date(entity.getTimestamp()));
                        voiceMemos.add(new VoiceMemoItem(voicePath, dateStr, entity.getMoodLevel()));
                    }
                }
            }

            if (voiceMemos.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        });
    }

    private void playVoiceMemo(int position) {
        VoiceMemoItem item = voiceMemos.get(position);

        // Stop current playback
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            if (currentlyPlayingPosition == position) {
                currentlyPlayingPosition = -1;
                adapter.setPlayingPosition(-1);
                return;
            }
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(item.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentlyPlayingPosition = position;
            adapter.setPlayingPosition(position);

            mediaPlayer.setOnCompletionListener(mp -> {
                currentlyPlayingPosition = -1;
                adapter.setPlayingPosition(-1);
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

    // Inner class for voice memo item
    public static class VoiceMemoItem {
        private String path;
        private String dateStr;
        private int moodLevel;

        public VoiceMemoItem(String path, String dateStr, int moodLevel) {
            this.path = path;
            this.dateStr = dateStr;
            this.moodLevel = moodLevel;
        }

        public String getPath() { return path; }
        public String getDateStr() { return dateStr; }
        public int getMoodLevel() { return moodLevel; }
    }
}
