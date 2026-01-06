package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.util.DateUtils;
import gui.ceng.mu.edu.mentalhealthjournal.util.MoodUtils;
import gui.ceng.mu.edu.mentalhealthjournal.util.OnboardingManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends BaseNavigationActivity {

    private RecentEntriesAdapter adapter;
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private JournalRepository repository;
    private TextView greetingText, streakText, userNameText;
    private ActivityResultLauncher<Intent> addEntryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.applyStoredTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new JournalRepository(this);
        addEntryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {});

        initViews();
        setupBottomNavigation(R.id.navigation_home);
        setupMoodClickListeners();
        setupRecyclerView();
        repository.getRecentEntriesLive(10).observe(this, this::updateEntries);
    }

    @Override
    protected int getCurrentNavigationItem() { return R.id.navigation_home; }

    private void initViews() {
        greetingText = findViewById(R.id.greeting_text);
        streakText = findViewById(R.id.streak_text);
        userNameText = findViewById(R.id.user_name_text);
        updateGreeting();
        updateUserName();
    }

    private void setupMoodClickListeners() {
        int[] moodViews = {R.id.mood_very_bad, R.id.mood_bad, R.id.mood_normal, R.id.mood_good, R.id.mood_very_good};
        for (int i = 0; i < moodViews.length; i++) {
            int moodLevel = i + 1;
            findViewById(moodViews[i]).setOnClickListener(v -> openAddEntry(moodLevel));
        }
        findViewById(R.id.btn_add_entry).setOnClickListener(v -> startActivity(new Intent(this, MoodSelectionActivity.class)));
    }

    private void openAddEntry(int moodLevel) {
        Intent intent = new Intent(this, AddEntryActivity.class);
        intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, moodLevel);
        addEntryLauncher.launch(intent);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recent_entries_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentEntriesAdapter(journalEntries, new RecentEntriesAdapter.OnEntryActionListener() {
            @Override
            public void onEditEntry(JournalEntry entry) {
                Intent intent = new Intent(MainActivity.this, AddEntryActivity.class);
                intent.putExtra(AddEntryActivity.EXTRA_ENTRY_ID, entry.getId());
                intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, entry.getMoodLevel());
                addEntryLauncher.launch(intent);
            }
            @Override
            public void onDeleteEntry(JournalEntry entry) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Delete Entry").setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Delete", (d, w) -> { repository.deleteById(entry.getId()); Toast.makeText(MainActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show(); })
                    .setNegativeButton("Cancel", null).show();
            }
        });
        recyclerView.setAdapter(adapter);
        findViewById(R.id.btn_view_all).setOnClickListener(v -> startActivity(new Intent(this, AllEntriesActivity.class)));
    }

    private void updateEntries(List<JournalEntryEntity> entities) {
        journalEntries.clear();
        if (entities != null) {
            for (JournalEntryEntity e : entities) {
                String title = e.getNote() != null && !e.getNote().isEmpty() 
                    ? (e.getNote().length() > 30 ? e.getNote().substring(0, 30) + "..." : e.getNote())
                    : (e.getEmotions() != null && !e.getEmotions().isEmpty() ? "Feeling " + e.getEmotions().get(0) : MoodUtils.getText(e.getMoodLevel()));
                journalEntries.add(new JournalEntry(e.getId(), title, DateUtils.getTimeAgo(e.getTimestamp()), 
                    e.getMoodIconResource(), e.getMoodBackgroundResource(), e.getMoodLevel()));
            }
        }
        adapter.notifyDataSetChanged();
        updateStreak(entities);
    }

    private void updateStreak(List<JournalEntryEntity> entities) {
        if (entities == null || entities.isEmpty()) { streakText.setText("0 days"); return; }
        Set<String> dates = new HashSet<>();
        for (JournalEntryEntity e : entities) dates.add(DateUtils.getDateKey(e.getTimestamp()));
        
        Calendar cal = Calendar.getInstance();
        int streak = 0;
        String today = DateUtils.getDateKey(cal.getTimeInMillis());
        
        if (dates.contains(today)) {
            streak = 1;
            cal.add(Calendar.DAY_OF_YEAR, -1);
            while (dates.contains(DateUtils.getDateKey(cal.getTimeInMillis()))) { streak++; cal.add(Calendar.DAY_OF_YEAR, -1); }
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            if (dates.contains(DateUtils.getDateKey(cal.getTimeInMillis()))) {
                streak = 1;
                cal.add(Calendar.DAY_OF_YEAR, -1);
                while (dates.contains(DateUtils.getDateKey(cal.getTimeInMillis()))) { streak++; cal.add(Calendar.DAY_OF_YEAR, -1); }
            }
        }
        streakText.setText(streak + " days");
    }

    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        greetingText.setText(hour < 12 ? "Good Morning," : hour < 17 ? "Good Afternoon," : "Good Evening,");
    }

    private void updateUserName() {
        userNameText.setText(new OnboardingManager(this).getUserName() + " 👋");
    }

    @Override
    protected void onResume() { super.onResume(); updateUserName(); }
}
