package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main activity displaying recent journal entries and mood selection.
 * Uses LiveData to observe database changes and automatically update UI.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecentEntriesAdapter adapter;
    private List<JournalEntry> journalEntries;
    
    private JournalRepository repository;
    private TextView greetingText;
    private TextView streakText;

    // Activity result launcher for AddEntryActivity
    private ActivityResultLauncher<Intent> addEntryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository
        repository = new JournalRepository(this);

        // Initialize activity result launcher
        addEntryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Entries will auto-update via LiveData observation
                }
        );

        initViews();
        setupBottomNavigation();
        setupMoodClickListeners();
        setupRecyclerView();
        observeEntries();
        updateGreeting();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greeting_text);
        streakText = findViewById(R.id.streak_text);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_calendar) {
                startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_stats) {
                startActivity(new Intent(getApplicationContext(), StatsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_more) {
                startActivity(new Intent(getApplicationContext(), MoreActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void setupMoodClickListeners() {
        // Get all mood containers
        View moodVeryGood = findViewById(R.id.mood_very_good);
        View moodGood = findViewById(R.id.mood_good);
        View moodNormal = findViewById(R.id.mood_normal);
        View moodBad = findViewById(R.id.mood_bad);
        View moodVeryBad = findViewById(R.id.mood_very_bad);

        // Set click listeners for each mood
        moodVeryGood.setOnClickListener(v -> openAddEntry(5));
        moodGood.setOnClickListener(v -> openAddEntry(4));
        moodNormal.setOnClickListener(v -> openAddEntry(3));
        moodBad.setOnClickListener(v -> openAddEntry(2));
        moodVeryBad.setOnClickListener(v -> openAddEntry(1));

        // Add Entry button
        MaterialButton btnAddEntry = findViewById(R.id.btn_add_entry);
        btnAddEntry.setOnClickListener(v -> openMoodSelection()); // Open mood selection first
    }

    private void openAddEntry(int moodLevel) {
        Intent intent = new Intent(this, AddEntryActivity.class);
        intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, moodLevel);
        addEntryLauncher.launch(intent);
    }
    
    private void openMoodSelection() {
        Intent intent = new Intent(this, MoodSelectionActivity.class);
        // Use current date
        startActivity(intent);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recent_entries_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);

        journalEntries = new ArrayList<>();
        adapter = new RecentEntriesAdapter(journalEntries);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Observe journal entries from database using LiveData.
     * This automatically updates the UI when entries change.
     */
    private void observeEntries() {
        LiveData<List<JournalEntryEntity>> entriesLiveData = repository.getRecentEntriesLive(10);
        
        entriesLiveData.observe(this, entities -> {
            journalEntries.clear();
            
            if (entities != null && !entities.isEmpty()) {
                for (JournalEntryEntity entity : entities) {
                    JournalEntry entry = convertEntityToJournalEntry(entity);
                    journalEntries.add(entry);
                }
            }
            
            adapter.notifyDataSetChanged();
            
            // Update streak display
            updateStreak(entities);
        });
    }

    /**
     * Convert JournalEntryEntity to JournalEntry for display
     */
    private JournalEntry convertEntityToJournalEntry(JournalEntryEntity entity) {
        String title = generateEntryTitle(entity);
        String timeAgo = getTimeAgo(entity.getTimestamp());
        int moodIcon = entity.getMoodIconResource();
        int moodBackground = entity.getMoodBackgroundResource();

        return new JournalEntry(title, timeAgo, moodIcon, moodBackground);
    }

    /**
     * Generate a title for the entry based on its content
     */
    private String generateEntryTitle(JournalEntryEntity entity) {
        // If there's a note, use the first part of it
        if (entity.getNote() != null && !entity.getNote().isEmpty()) {
            String note = entity.getNote();
            if (note.length() > 30) {
                return note.substring(0, 30) + "...";
            }
            return note;
        }

        // Otherwise, generate based on emotions
        List<String> emotions = entity.getEmotions();
        if (emotions != null && !emotions.isEmpty()) {
            return "Feeling " + emotions.get(0);
        }

        // Default titles based on mood
        switch (entity.getMoodLevel()) {
            case 5:
                return "Feeling Great!";
            case 4:
                return "A Good Day";
            case 3:
                return "Normal Day";
            case 2:
                return "Could Be Better";
            case 1:
            default:
                return "Tough Day";
        }
    }

    /**
     * Get relative time string (e.g., "2 hours ago")
     */
    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else if (days > 1) {
            return days + " days ago";
        } else if (days == 1) {
            return "Yesterday";
        } else if (hours > 1) {
            return hours + " hours ago";
        } else if (hours == 1) {
            return "1 hour ago";
        } else if (minutes > 1) {
            return minutes + " minutes ago";
        } else {
            return "Just now";
        }
    }

    /**
     * Update the streak counter based on consecutive days with entries
     */
    private void updateStreak(List<JournalEntryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            streakText.setText("0 days");
            return;
        }

        // Calculate streak - count consecutive days with entries
        int streak = 0;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar entryDate = Calendar.getInstance();
        Calendar checkDate = (Calendar) today.clone();

        boolean hasEntryToday = false;
        for (JournalEntryEntity entity : entities) {
            entryDate.setTimeInMillis(entity.getTimestamp());
            if (isSameDay(entryDate, today)) {
                hasEntryToday = true;
                break;
            }
        }

        if (hasEntryToday) {
            streak = 1;
            checkDate.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Check previous days
        while (true) {
            boolean foundEntry = false;
            for (JournalEntryEntity entity : entities) {
                entryDate.setTimeInMillis(entity.getTimestamp());
                if (isSameDay(entryDate, checkDate)) {
                    foundEntry = true;
                    break;
                }
            }
            if (foundEntry) {
                streak++;
                checkDate.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        streakText.setText(streak + " days");
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Update greeting based on time of day
     */
    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = "Good Morning,";
        } else if (hour < 17) {
            greeting = "Good Afternoon,";
        } else {
            greeting = "Good Evening,";
        }

        greetingText.setText(greeting);
    }
}
