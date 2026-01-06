package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

/**
 * Activity displaying all journal entries in a scrollable list.
 * Newest entries appear at the top.
 * Can be filtered by date when opened from calendar.
 */
public class AllEntriesActivity extends AppCompatActivity {

    public static final String EXTRA_FILTER_DATE = "filter_date";
    public static final String EXTRA_FILTER_DATE_MILLIS_START = "filter_date_millis_start";
    public static final String EXTRA_FILTER_DATE_MILLIS_END = "filter_date_millis_end";

    private RecyclerView recyclerView;
    private RecentEntriesAdapter adapter;
    private List<JournalEntry> journalEntries;
    private JournalRepository repository;
    private Handler mainHandler;
    private TextView emptyText;
    private TextView titleText;
    
    private String filterDate;
    private long filterStartMillis = -1;
    private long filterEndMillis = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_entries);

        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Check for date filter from intent
        filterDate = getIntent().getStringExtra(EXTRA_FILTER_DATE);
        filterStartMillis = getIntent().getLongExtra(EXTRA_FILTER_DATE_MILLIS_START, -1);
        filterEndMillis = getIntent().getLongExtra(EXTRA_FILTER_DATE_MILLIS_END, -1);

        initViews();
        loadAllEntries();
    }

    private void initViews() {
        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        emptyText = findViewById(R.id.empty_text);
        titleText = findViewById(R.id.title_text);
        recyclerView = findViewById(R.id.entries_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Update title if filtering by date
        if (filterDate != null && titleText != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(filterDate);
                if (date != null) {
                    titleText.setText("Entries for " + outputFormat.format(date));
                }
            } catch (Exception e) {
                titleText.setText("Entries for " + filterDate);
            }
        }

        journalEntries = new ArrayList<>();
        adapter = new RecentEntriesAdapter(journalEntries, new RecentEntriesAdapter.OnEntryActionListener() {
            @Override
            public void onEditEntry(JournalEntry entry) {
                Intent intent = new Intent(AllEntriesActivity.this, AddEntryActivity.class);
                intent.putExtra(AddEntryActivity.EXTRA_ENTRY_ID, entry.getId());
                intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, entry.getMoodLevel());
                startActivity(intent);
            }

            @Override
            public void onDeleteEntry(JournalEntry entry) {
                new AlertDialog.Builder(AllEntriesActivity.this)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        repository.deleteById(entry.getId());
                        Toast.makeText(AllEntriesActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show();
                        loadAllEntries();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });

        // Set click listener for viewing entry details
        adapter.setOnItemClickListener(entry -> {
            Intent intent = new Intent(AllEntriesActivity.this, EntryViewActivity.class);
            intent.putExtra(EntryViewActivity.EXTRA_ENTRY_ID, entry.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void loadAllEntries() {
        // Check if we need to filter by date
        if (filterStartMillis > 0 && filterEndMillis > 0) {
            repository.getEntriesByDateRange(filterStartMillis, filterEndMillis).observe(this, this::updateEntriesList);
        } else {
            repository.getAllEntries().observe(this, this::updateEntriesList);
        }
    }
    
    private void updateEntriesList(List<JournalEntryEntity> entities) {
        journalEntries.clear();

        if (entities != null && !entities.isEmpty()) {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            for (JournalEntryEntity entity : entities) {
                JournalEntry entry = convertEntityToJournalEntry(entity);
                journalEntries.add(entry);
            }
        } else {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }

    private JournalEntry convertEntityToJournalEntry(JournalEntryEntity entity) {
        String title = generateEntryTitle(entity);
        String timeAgo = getTimeAgo(entity.getTimestamp());
        int moodIcon = entity.getMoodIconResource();
        int moodBackground = entity.getMoodBackgroundResource();

        return new JournalEntry(entity.getId(), title, timeAgo, moodIcon, moodBackground, entity.getMoodLevel());
    }

    private String generateEntryTitle(JournalEntryEntity entity) {
        if (entity.getNote() != null && !entity.getNote().isEmpty()) {
            String note = entity.getNote();
            if (note.length() > 30) {
                return note.substring(0, 30) + "...";
            }
            return note;
        }

        List<String> emotions = entity.getEmotions();
        if (emotions != null && !emotions.isEmpty()) {
            return "Feeling " + emotions.get(0);
        }

        switch (entity.getMoodLevel()) {
            case 5: return "Feeling Great!";
            case 4: return "A Good Day";
            case 3: return "Normal Day";
            case 2: return "Could Be Better";
            case 1:
            default: return "Tough Day";
        }
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAllEntries();
    }
}
