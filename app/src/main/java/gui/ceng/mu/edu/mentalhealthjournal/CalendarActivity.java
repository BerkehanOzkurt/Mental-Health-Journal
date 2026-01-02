package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

public class CalendarActivity extends AppCompatActivity {

    private GridLayout calendarGrid;
    private TextView tvMonthYear;
    private MaterialButton btnPrevMonth, btnNextMonth;
    
    private Calendar currentCalendar;
    private JournalRepository repository;
    private Map<String, Integer> moodByDate = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        
        // Initialize repository
        repository = new JournalRepository(getApplication());
        
        // Initialize calendar to current month
        currentCalendar = Calendar.getInstance();

        // Initialize views
        calendarGrid = findViewById(R.id.calendar_grid);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);

        // Set up navigation buttons
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            loadCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            loadCalendar();
        });

        // Load calendar
        loadCalendar();

        // Bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_calendar);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_calendar) {
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
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCalendar();
    }

    private void loadCalendar() {
        // Update month/year display
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));
        
        // Get first and last day of month for database query
        Calendar firstDay = (Calendar) currentCalendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        firstDay.set(Calendar.HOUR_OF_DAY, 0);
        firstDay.set(Calendar.MINUTE, 0);
        firstDay.set(Calendar.SECOND, 0);
        firstDay.set(Calendar.MILLISECOND, 0);
        
        Calendar lastDay = (Calendar) currentCalendar.clone();
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        lastDay.set(Calendar.HOUR_OF_DAY, 23);
        lastDay.set(Calendar.MINUTE, 59);
        lastDay.set(Calendar.SECOND, 59);
        
        // Load entries for this month from database
        LiveData<List<JournalEntryEntity>> entriesLiveData = 
            repository.getEntriesByDateRange(firstDay.getTimeInMillis(), lastDay.getTimeInMillis());
        
        entriesLiveData.observe(this, entries -> {
            moodByDate.clear();
            if (entries != null) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                for (JournalEntryEntity entry : entries) {
                    String dateKey = dayFormat.format(entry.getTimestamp());
                    moodByDate.put(dateKey, entry.getMoodLevel());
                }
            }
            buildCalendarGrid();
        });
    }

    private void buildCalendarGrid() {
        calendarGrid.removeAllViews();
        
        // Get first day of month
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // Convert to Monday-based (1=Monday, 7=Sunday)
        int offset = (firstDayOfWeek + 5) % 7;
        
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalCells = offset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);
        
        calendarGrid.setRowCount(rows);
        
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        int dayCounter = 1;
        for (int i = 0; i < rows * 7; i++) {
            View dayView = LayoutInflater.from(this).inflate(R.layout.item_calendar_day, calendarGrid, false);
            
            TextView tvDayNumber = dayView.findViewById(R.id.tv_day_number);
            MaterialButton btnAddEntry = dayView.findViewById(R.id.btn_add_entry);
            ImageView imgEmoji = dayView.findViewById(R.id.img_emoji);
            
            if (i < offset || dayCounter > daysInMonth) {
                // Empty cell
                dayView.setVisibility(View.INVISIBLE);
            } else {
                final int day = dayCounter;
                tvDayNumber.setText(String.valueOf(day));
                
                // Check if there's an entry for this day
                cal.set(Calendar.DAY_OF_MONTH, day);
                String dateKey = dayFormat.format(cal.getTime());
                
                if (moodByDate.containsKey(dateKey)) {
                    // Show emoji for this mood
                    int mood = moodByDate.get(dateKey);
                    btnAddEntry.setVisibility(View.GONE);
                    imgEmoji.setVisibility(View.VISIBLE);
                    imgEmoji.setImageResource(getMoodDrawable(mood));
                    
                    // Click to edit entry
                    imgEmoji.setOnClickListener(v -> openAddEntry(day));
                } else {
                    // Show add button
                    btnAddEntry.setVisibility(View.VISIBLE);
                    imgEmoji.setVisibility(View.GONE);
                    btnAddEntry.setOnClickListener(v -> openAddEntry(day));
                }
                
                dayCounter++;
            }
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 7, 1f);
            params.rowSpec = GridLayout.spec(i / 7);
            dayView.setLayoutParams(params);
            
            calendarGrid.addView(dayView);
        }
    }
    
    private int getMoodDrawable(int mood) {
        switch (mood) {
            case 5: return R.drawable.face1; // Very Good
            case 4: return R.drawable.face2; // Good
            case 3: return R.drawable.face3; // Normal
            case 2: return R.drawable.face4; // Bad
            case 1: return R.drawable.face5; // Very Bad
            default: return R.drawable.face3;
        }
    }
    
    private void openAddEntry(int day) {
        Intent intent = new Intent(this, MoodSelectionActivity.class);
        intent.putExtra(MoodSelectionActivity.EXTRA_DAY, day);
        intent.putExtra(MoodSelectionActivity.EXTRA_MONTH, currentCalendar.get(Calendar.MONTH));
        intent.putExtra(MoodSelectionActivity.EXTRA_YEAR, currentCalendar.get(Calendar.YEAR));
        startActivity(intent);
    }
}
