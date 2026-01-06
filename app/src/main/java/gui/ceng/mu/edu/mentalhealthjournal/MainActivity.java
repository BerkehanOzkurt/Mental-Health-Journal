package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
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
    private TextView userNameText;

    // Activity result launcher for AddEntryActivity
    private ActivityResultLauncher<Intent> addEntryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply stored theme before calling super
        SettingsActivity.applyStoredTheme(this);
        
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
        updateUserName();
    }

    private void initViews() {
        greetingText = findViewById(R.id.greeting_text);
        streakText = findViewById(R.id.streak_text);
        userNameText = findViewById(R.id.user_name_text);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                return true;
            } else if (id == R.id.navigation_calendar) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_stats) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_more) {
                Intent intent = new Intent(getApplicationContext(), MoreActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        repository.deleteById(entry.getId());
                        Toast.makeText(MainActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        
        recyclerView.setAdapter(adapter);
        
        // View All button
        MaterialButton btnViewAll = findViewById(R.id.btn_view_all);
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllEntriesActivity.class);
            startActivity(intent);
        });
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

        return new JournalEntry(entity.getId(), title, timeAgo, moodIcon, moodBackground, entity.getMoodLevel());
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
     * Calculates from today backwards - counts consecutive days with entries
     */
    private void updateStreak(List<JournalEntryEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            streakText.setText("0 days");
            return;
        }

        // Create a set of dates that have entries
        java.util.Set<String> datesWithEntries = new java.util.HashSet<>();
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US);
        
        for (JournalEntryEntity entity : entities) {
            String dateKey = dateFormat.format(new java.util.Date(entity.getTimestamp()));
            datesWithEntries.add(dateKey);
        }

        // Start from today and go backwards
        Calendar checkDate = Calendar.getInstance();
        int streak = 0;
        
        // Check today first
        String todayKey = dateFormat.format(checkDate.getTime());
        if (datesWithEntries.contains(todayKey)) {
            streak = 1;
            checkDate.add(Calendar.DAY_OF_YEAR, -1);
            
            // Continue checking previous days
            while (true) {
                String dateKey = dateFormat.format(checkDate.getTime());
                if (datesWithEntries.contains(dateKey)) {
                    streak++;
                    checkDate.add(Calendar.DAY_OF_YEAR, -1);
                } else {
                    break;
                }
            }
        } else {
            // No entry today, check if yesterday has entry and start from there
            checkDate.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayKey = dateFormat.format(checkDate.getTime());
            
            if (datesWithEntries.contains(yesterdayKey)) {
                streak = 1;
                checkDate.add(Calendar.DAY_OF_YEAR, -1);
                
                while (true) {
                    String dateKey = dateFormat.format(checkDate.getTime());
                    if (datesWithEntries.contains(dateKey)) {
                        streak++;
                        checkDate.add(Calendar.DAY_OF_YEAR, -1);
                    } else {
                        break;
                    }
                }
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

    /**
     * Update user name from SharedPreferences using OnboardingManager
     */
    private void updateUserName() {
        gui.ceng.mu.edu.mentalhealthjournal.util.OnboardingManager onboardingManager = 
                new gui.ceng.mu.edu.mentalhealthjournal.util.OnboardingManager(this);
        String userName = onboardingManager.getUserName();
        userNameText.setText(userName + " 👋");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bottom navigation selection when returning to this activity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        // Update user name in case it was changed in settings
        updateUserName();
    }
}
