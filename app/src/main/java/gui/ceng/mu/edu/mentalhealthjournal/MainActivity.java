package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecentEntriesAdapter adapter;
    private List<JournalEntry> journalEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bottom Navigation Setup
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

        // RecyclerView Setup
        recyclerView = findViewById(R.id.recent_entries_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(true);

        journalEntries = new ArrayList<>();
        loadDummyEntries();

        adapter = new RecentEntriesAdapter(journalEntries);
        recyclerView.setAdapter(adapter);
    }

    private void loadDummyEntries() {
        journalEntries.add(new JournalEntry("Feeling Great Today", "2 hours ago", R.drawable.face1, R.drawable.emoji_background_very_good));
        journalEntries.add(new JournalEntry("A productive morning", "5 hours ago", R.drawable.face2, R.drawable.emoji_background_good));
        journalEntries.add(new JournalEntry("Could be better", "Yesterday", R.drawable.face4, R.drawable.emoji_background_bad));
        journalEntries.add(new JournalEntry("Just a normal day", "2 days ago", R.drawable.face3, R.drawable.emoji_background_normal));
        journalEntries.add(new JournalEntry("Rough start", "3 days ago", R.drawable.face5, R.drawable.emoji_background_very_bad));
        journalEntries.add(new JournalEntry("Mid-week reflection", "4 days ago", R.drawable.face3, R.drawable.emoji_background_normal));
        journalEntries.add(new JournalEntry("Amazing weekend!", "Last Saturday", R.drawable.face1, R.drawable.emoji_background_very_good));
        journalEntries.add(new JournalEntry("Feeling a bit blue", "Last Friday", R.drawable.face4, R.drawable.emoji_background_bad));
        journalEntries.add(new JournalEntry("Steady progress", "Last Thursday", R.drawable.face2, R.drawable.emoji_background_good));
    }
}
