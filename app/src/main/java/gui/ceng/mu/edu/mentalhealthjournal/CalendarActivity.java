package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private ImageButton btnPrevMonth, btnNextMonth;
    private CardView selectedDayCard;
    private ImageView selectedDayEmoji;
    private TextView selectedDayDate, selectedDayMood, btnViewEntry;
    
    private Calendar currentCalendar;
    private Calendar todayCalendar;
    private JournalRepository repository;
    private Map<String, List<JournalEntryEntity>> entriesByDate = new HashMap<>();
    private int selectedDay = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        
        // Initialize repository
        repository = new JournalRepository(getApplication());
        
        // Initialize calendar to current month
        currentCalendar = Calendar.getInstance();
        todayCalendar = Calendar.getInstance();

        // Initialize views
        calendarGrid = findViewById(R.id.calendar_grid);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        
        // Selected day card views
        selectedDayCard = findViewById(R.id.selected_day_card);
        selectedDayEmoji = findViewById(R.id.selected_day_emoji);
        selectedDayDate = findViewById(R.id.selected_day_date);
        selectedDayMood = findViewById(R.id.selected_day_mood);
        btnViewEntry = findViewById(R.id.btn_view_entry);

        // Set up navigation buttons
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            selectedDay = -1;
            selectedDayCard.setVisibility(View.GONE);
            loadCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            // Check if we can go to next month (only if it's not future)
            Calendar nextMonth = (Calendar) currentCalendar.clone();
            nextMonth.add(Calendar.MONTH, 1);
            
            if (nextMonth.get(Calendar.YEAR) > todayCalendar.get(Calendar.YEAR) ||
                (nextMonth.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && 
                 nextMonth.get(Calendar.MONTH) > todayCalendar.get(Calendar.MONTH))) {
                // Don't allow going to future months
                Toast.makeText(this, "Cannot go to future months", Toast.LENGTH_SHORT).show();
                return;
            }
            
            selectedDay = -1;
            selectedDayCard.setVisibility(View.GONE);
            
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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_calendar) {
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
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bottom navigation selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_calendar);
        // Reload calendar data
        loadCalendar();
        updateNextButtonState();
    }

    private void updateNextButtonState() {
        // Disable next button if we're at current month
        Calendar nextMonth = (Calendar) currentCalendar.clone();
        nextMonth.add(Calendar.MONTH, 1);
        
        boolean isFutureMonth = nextMonth.get(Calendar.YEAR) > todayCalendar.get(Calendar.YEAR) ||
            (nextMonth.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) && 
             nextMonth.get(Calendar.MONTH) > todayCalendar.get(Calendar.MONTH));
        
        btnNextMonth.setAlpha(isFutureMonth ? 0.3f : 1.0f);
    }

    private void loadCalendar() {
        // Update next button state
        updateNextButtonState();
        
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
            entriesByDate.clear();
            if (entries != null) {
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                for (JournalEntryEntity entry : entries) {
                    String dateKey = dayFormat.format(entry.getTimestamp());
                    if (!entriesByDate.containsKey(dateKey)) {
                        entriesByDate.put(dateKey, new ArrayList<>());
                    }
                    entriesByDate.get(dateKey).add(entry);
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
            ImageView btnAddEntry = dayView.findViewById(R.id.btn_add_entry);
            ImageView imgEmoji = dayView.findViewById(R.id.img_emoji);
            View todayIndicator = dayView.findViewById(R.id.today_indicator);
            
            if (i < offset || dayCounter > daysInMonth) {
                // Empty cell
                dayView.setVisibility(View.INVISIBLE);
            } else {
                final int day = dayCounter;
                tvDayNumber.setText(String.valueOf(day));
                
                // Check if this day is today
                cal.set(Calendar.DAY_OF_MONTH, day);
                boolean isToday = isToday(cal);
                if (isToday) {
                    tvDayNumber.setTextColor(getResources().getColor(R.color.very_good, null));
                    todayIndicator.setVisibility(View.VISIBLE);
                }
                
                // Check if this day is in the future
                boolean isFutureDay = isFutureDate(cal);
                
                // Check if there are entries for this day
                String dateKey = dayFormat.format(cal.getTime());
                
                if (entriesByDate.containsKey(dateKey) && !entriesByDate.get(dateKey).isEmpty()) {
                    List<JournalEntryEntity> dayEntries = entriesByDate.get(dateKey);
                    // Show emoji for the most recent mood (first in list as they're sorted by timestamp desc)
                    int mood = dayEntries.get(0).getMoodLevel();
                    int entryCount = dayEntries.size();
                    
                    btnAddEntry.setVisibility(View.GONE);
                    imgEmoji.setVisibility(View.VISIBLE);
                    imgEmoji.setImageResource(getMoodDrawable(mood));
                    
                    // Click to show entry info
                    imgEmoji.setOnClickListener(v -> {
                        showSelectedDayInfo(day, dateKey, dayEntries);
                    });
                } else if (isFutureDay) {
                    // Future day - show disabled state
                    btnAddEntry.setVisibility(View.VISIBLE);
                    btnAddEntry.setAlpha(0.15f);
                    imgEmoji.setVisibility(View.GONE);
                    btnAddEntry.setOnClickListener(null);
                    tvDayNumber.setAlpha(0.4f);
                } else {
                    // Past or today - show add button
                    btnAddEntry.setVisibility(View.VISIBLE);
                    btnAddEntry.setAlpha(0.5f);
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
    
    private boolean isToday(Calendar date) {
        return date.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
               date.get(Calendar.MONTH) == todayCalendar.get(Calendar.MONTH) &&
               date.get(Calendar.DAY_OF_MONTH) == todayCalendar.get(Calendar.DAY_OF_MONTH);
    }
    
    private void showSelectedDayInfo(int day, String dateKey, List<JournalEntryEntity> entries) {
        selectedDay = day;
        selectedDayCard.setVisibility(View.VISIBLE);
        
        // Set date text
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        selectedDayDate.setText(dateFormat.format(cal.getTime()));
        
        // Get the most recent entry's mood
        int mood = entries.get(0).getMoodLevel();
        int entryCount = entries.size();
        
        // Set mood emoji and text
        selectedDayEmoji.setImageResource(getMoodDrawable(mood));
        if (entryCount > 1) {
            selectedDayMood.setText(entryCount + " entries");
        } else {
            selectedDayMood.setText(getMoodText(mood));
        }
        
        // Set view button click to open entries for this day
        btnViewEntry.setOnClickListener(v -> {
            openDayEntries(day, dateKey);
        });
    }
    
    private void openDayEntries(int day, String dateKey) {
        // Open AllEntriesActivity with date filter
        Intent intent = new Intent(this, AllEntriesActivity.class);
        intent.putExtra(AllEntriesActivity.EXTRA_FILTER_DATE, dateKey);
        
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        intent.putExtra(AllEntriesActivity.EXTRA_FILTER_DATE_MILLIS_START, getStartOfDay(cal));
        intent.putExtra(AllEntriesActivity.EXTRA_FILTER_DATE_MILLIS_END, getEndOfDay(cal));
        startActivity(intent);
    }
    
    private long getStartOfDay(Calendar cal) {
        Calendar start = (Calendar) cal.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTimeInMillis();
    }
    
    private long getEndOfDay(Calendar cal) {
        Calendar end = (Calendar) cal.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTimeInMillis();
    }
    
    private String getMoodText(int mood) {
        switch (mood) {
            case 5: return "Feeling Great";
            case 4: return "Feeling Good";
            case 3: return "Feeling Okay";
            case 2: return "Feeling Bad";
            case 1: return "Feeling Awful";
            default: return "Unknown";
        }
    }
    
    private boolean isFutureDate(Calendar date) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar checkDate = (Calendar) date.clone();
        checkDate.set(Calendar.HOUR_OF_DAY, 0);
        checkDate.set(Calendar.MINUTE, 0);
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);
        
        return checkDate.after(today);
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
