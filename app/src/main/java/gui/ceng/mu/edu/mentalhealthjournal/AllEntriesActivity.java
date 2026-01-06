package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.util.DateUtils;
import gui.ceng.mu.edu.mentalhealthjournal.util.MoodUtils;

public class AllEntriesActivity extends AppCompatActivity {

    public static final String EXTRA_FILTER_DATE = "filter_date";
    public static final String EXTRA_FILTER_DATE_MILLIS_START = "filter_date_millis_start";
    public static final String EXTRA_FILTER_DATE_MILLIS_END = "filter_date_millis_end";

    private RecyclerView recyclerView;
    private RecentEntriesAdapter adapter;
    private List<JournalEntry> journalEntries = new ArrayList<>();
    private JournalRepository repository;
    private TextView emptyText, titleText, filterSummaryText;
    private Chip chipHasPhoto, chipHasVoice, chipMood1, chipMood2, chipMood3, chipMood4, chipMood5;

    private String currentSearchQuery = "";
    private boolean filterHasPhoto = false, filterHasVoice = false;
    private Set<Integer> selectedMoodLevels = new HashSet<>();
    private List<JournalEntryEntity> allEntriesCache = new ArrayList<>();
    private long filterStartMillis = -1, filterEndMillis = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_entries);
        repository = new JournalRepository(this);
        filterStartMillis = getIntent().getLongExtra(EXTRA_FILTER_DATE_MILLIS_START, -1);
        filterEndMillis = getIntent().getLongExtra(EXTRA_FILTER_DATE_MILLIS_END, -1);
        String filterDate = getIntent().getStringExtra(EXTRA_FILTER_DATE);
        
        initViews(filterDate);
        setupSearch();
        setupFilterChips();
        loadAllEntries();
    }

    private void initViews(String filterDate) {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        emptyText = findViewById(R.id.empty_text);
        titleText = findViewById(R.id.title_text);
        filterSummaryText = findViewById(R.id.filter_summary_text);
        recyclerView = findViewById(R.id.entries_recyclerview);
        chipHasPhoto = findViewById(R.id.chip_has_photo);
        chipHasVoice = findViewById(R.id.chip_has_voice);
        chipMood1 = findViewById(R.id.chip_mood_1);
        chipMood2 = findViewById(R.id.chip_mood_2);
        chipMood3 = findViewById(R.id.chip_mood_3);
        chipMood4 = findViewById(R.id.chip_mood_4);
        chipMood5 = findViewById(R.id.chip_mood_5);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (filterDate != null) {
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(filterDate);
                if (date != null) titleText.setText("Entries for " + new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date));
            } catch (Exception e) { titleText.setText("Entries for " + filterDate); }
        }

        adapter = new RecentEntriesAdapter(journalEntries, new RecentEntriesAdapter.OnEntryActionListener() {
            @Override public void onEditEntry(JournalEntry entry) {
                Intent intent = new Intent(AllEntriesActivity.this, AddEntryActivity.class);
                intent.putExtra(AddEntryActivity.EXTRA_ENTRY_ID, entry.getId());
                intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, entry.getMoodLevel());
                startActivity(intent);
            }
            @Override public void onDeleteEntry(JournalEntry entry) {
                new AlertDialog.Builder(AllEntriesActivity.this)
                    .setTitle("Delete Entry").setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Delete", (d, w) -> { repository.deleteById(entry.getId()); Toast.makeText(AllEntriesActivity.this, "Entry deleted", Toast.LENGTH_SHORT).show(); loadAllEntries(); })
                    .setNegativeButton("Cancel", null).show();
            }
        });
        adapter.setOnItemClickListener(e -> { Intent i = new Intent(this, EntryViewActivity.class); i.putExtra(EntryViewActivity.EXTRA_ENTRY_ID, e.getId()); startActivity(i); });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { currentSearchQuery = q.trim(); applyFilters(); return true; }
            @Override public boolean onQueryTextChange(String q) { currentSearchQuery = q.trim(); applyFilters(); return true; }
        });
    }

    private void setupFilterChips() {
        chipHasPhoto.setOnCheckedChangeListener((b, c) -> { filterHasPhoto = c; applyFilters(); });
        chipHasVoice.setOnCheckedChangeListener((b, c) -> { filterHasVoice = c; applyFilters(); });
        chipMood1.setOnCheckedChangeListener((b, c) -> updateMoodFilter(1, c));
        chipMood2.setOnCheckedChangeListener((b, c) -> updateMoodFilter(2, c));
        chipMood3.setOnCheckedChangeListener((b, c) -> updateMoodFilter(3, c));
        chipMood4.setOnCheckedChangeListener((b, c) -> updateMoodFilter(4, c));
        chipMood5.setOnCheckedChangeListener((b, c) -> updateMoodFilter(5, c));
    }

    private void updateMoodFilter(int level, boolean selected) {
        if (selected) selectedMoodLevels.add(level); else selectedMoodLevels.remove(level);
        applyFilters();
    }

    private void loadAllEntries() {
        if (filterStartMillis > 0 && filterEndMillis > 0) {
            repository.getEntriesByDateRange(filterStartMillis, filterEndMillis).observe(this, e -> { allEntriesCache.clear(); if (e != null) allEntriesCache.addAll(e); applyFilters(); });
        } else {
            repository.getAllEntries().observe(this, e -> { allEntriesCache.clear(); if (e != null) allEntriesCache.addAll(e); applyFilters(); });
        }
    }

    private void applyFilters() {
        List<JournalEntryEntity> filtered = new ArrayList<>();
        for (JournalEntryEntity e : allEntriesCache) if (matchesAllFilters(e)) filtered.add(e);
        updateEntriesList(filtered);
        updateFilterSummary();
    }

    private boolean matchesAllFilters(JournalEntryEntity e) {
        if (!currentSearchQuery.isEmpty()) {
            String q = currentSearchQuery.toLowerCase();
            boolean match = (e.getNote() != null && e.getNote().toLowerCase().contains(q)) ||
                (e.getEmotions() != null && e.getEmotions().toString().toLowerCase().contains(q)) ||
                (e.getActivities() != null && e.getActivities().toString().toLowerCase().contains(q));
            if (!match) return false;
        }
        if (filterHasPhoto && (e.getPhotoPath() == null || e.getPhotoPath().isEmpty())) return false;
        if (filterHasVoice && (e.getVoiceMemoPath() == null || e.getVoiceMemoPath().isEmpty())) return false;
        if (!selectedMoodLevels.isEmpty() && !selectedMoodLevels.contains(e.getMoodLevel())) return false;
        return true;
    }

    private void updateFilterSummary() {
        List<String> filters = new ArrayList<>();
        if (!currentSearchQuery.isEmpty()) filters.add("\"" + currentSearchQuery + "\"");
        if (filterHasPhoto) filters.add("Has Photo");
        if (filterHasVoice) filters.add("Has Voice");
        if (!selectedMoodLevels.isEmpty()) {
            StringBuilder sb = new StringBuilder("Mood: ");
            for (int l : selectedMoodLevels) sb.append(MoodUtils.getLabel(l)).append(" ");
            filters.add(sb.toString().trim());
        }
        filterSummaryText.setVisibility(filters.isEmpty() ? View.GONE : View.VISIBLE);
        if (!filters.isEmpty()) filterSummaryText.setText("Filters: " + String.join(" • ", filters) + " (" + journalEntries.size() + " results)");
    }

    private void updateEntriesList(List<JournalEntryEntity> entities) {
        journalEntries.clear();
        if (entities != null && !entities.isEmpty()) {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            for (JournalEntryEntity e : entities) {
                String title = e.getNote() != null && !e.getNote().isEmpty() 
                    ? (e.getNote().length() > 30 ? e.getNote().substring(0, 30) + "..." : e.getNote())
                    : (e.getEmotions() != null && !e.getEmotions().isEmpty() ? "Feeling " + e.getEmotions().get(0) : MoodUtils.getText(e.getMoodLevel()));
                journalEntries.add(new JournalEntry(e.getId(), title, DateUtils.getTimeAgo(e.getTimestamp()), e.getMoodIconResource(), e.getMoodBackgroundResource(), e.getMoodLevel()));
            }
        } else {
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyText.setText(hasActiveFilters() ? "No entries match your filters.\nTry adjusting your search." : "No entries yet.\nStart tracking your mood!");
        }
        adapter.notifyDataSetChanged();
    }

    private boolean hasActiveFilters() { return !currentSearchQuery.isEmpty() || filterHasPhoto || filterHasVoice || !selectedMoodLevels.isEmpty(); }

    @Override protected void onResume() { super.onResume(); loadAllEntries(); }
}
