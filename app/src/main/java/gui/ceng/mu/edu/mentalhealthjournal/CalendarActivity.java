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

import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.util.DateUtils;
import gui.ceng.mu.edu.mentalhealthjournal.util.MoodUtils;

/**
 * Calendar activity displaying journal entries by date.
 * Uses BaseNavigationActivity for common navigation logic.
 */
public class CalendarActivity extends BaseNavigationActivity {

    private GridLayout calendarGrid;
    private TextView tvMonthYear;
    private ImageButton btnPrevMonth, btnNextMonth;
    private CardView selectedDayCard;
    private ImageView selectedDayEmoji;
    private TextView selectedDayDate, selectedDayMood, btnViewEntry;
    
    private Calendar currentCalendar;
    private JournalRepository repository;
    private Map<String, List<JournalEntryEntity>> entriesByDate = new HashMap<>();
    private int selectedDay = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        
        repository = new JournalRepository(getApplication());
        currentCalendar = Calendar.getInstance();

        initViews();
        setupNavigationButtons();
        setupBottomNavigation(R.id.navigation_calendar);
        loadCalendar();
    }

    @Override
    protected int getCurrentNavigationItem() {
        return R.id.navigation_calendar;
    }

    private void initViews() {
        calendarGrid = findViewById(R.id.calendar_grid);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        
        selectedDayCard = findViewById(R.id.selected_day_card);
        selectedDayEmoji = findViewById(R.id.selected_day_emoji);
        selectedDayDate = findViewById(R.id.selected_day_date);
        selectedDayMood = findViewById(R.id.selected_day_mood);
        btnViewEntry = findViewById(R.id.btn_view_entry);
    }

    private void setupNavigationButtons() {
        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            selectedDay = -1;
            selectedDayCard.setVisibility(View.GONE);
            loadCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            Calendar nextMonth = (Calendar) currentCalendar.clone();
            nextMonth.add(Calendar.MONTH, 1);
            
            if (DateUtils.isFutureMonth(nextMonth.get(Calendar.YEAR), nextMonth.get(Calendar.MONTH))) {
                Toast.makeText(this, "Cannot go to future months", Toast.LENGTH_SHORT).show();
                return;
            }
            
            selectedDay = -1;
            selectedDayCard.setVisibility(View.GONE);
            currentCalendar.add(Calendar.MONTH, 1);
            loadCalendar();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCalendar();
        updateNextButtonState();
    }

    private void updateNextButtonState() {
        Calendar nextMonth = (Calendar) currentCalendar.clone();
        nextMonth.add(Calendar.MONTH, 1);
        boolean isFutureMonth = DateUtils.isFutureMonth(nextMonth.get(Calendar.YEAR), nextMonth.get(Calendar.MONTH));
        btnNextMonth.setAlpha(isFutureMonth ? 0.3f : 1.0f);
    }

    private void loadCalendar() {
        updateNextButtonState();
        tvMonthYear.setText(DateUtils.format(currentCalendar, DateUtils.PATTERN_MONTH_YEAR));
        
        Calendar firstDay = (Calendar) currentCalendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        long startTime = DateUtils.getStartOfDay(firstDay);
        
        Calendar lastDay = (Calendar) currentCalendar.clone();
        lastDay.set(Calendar.DAY_OF_MONTH, lastDay.getActualMaximum(Calendar.DAY_OF_MONTH));
        long endTime = DateUtils.getEndOfDay(lastDay);
        
        LiveData<List<JournalEntryEntity>> entriesLiveData = 
            repository.getEntriesByDateRange(startTime, endTime);
        
        entriesLiveData.observe(this, entries -> {
            entriesByDate.clear();
            if (entries != null) {
                for (JournalEntryEntity entry : entries) {
                    String dateKey = DateUtils.getDateKey(entry.getTimestamp());
                    entriesByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(entry);
                }
            }
            buildCalendarGrid();
        });
    }

    private void buildCalendarGrid() {
        calendarGrid.removeAllViews();
        
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek + 5) % 7; // Monday-based
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int totalCells = offset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);
        
        calendarGrid.setRowCount(rows);
        
        int dayCounter = 1;
        for (int i = 0; i < rows * 7; i++) {
            View dayView = LayoutInflater.from(this).inflate(R.layout.item_calendar_day, calendarGrid, false);
            
            TextView tvDayNumber = dayView.findViewById(R.id.tv_day_number);
            ImageView btnAddEntry = dayView.findViewById(R.id.btn_add_entry);
            ImageView imgEmoji = dayView.findViewById(R.id.img_emoji);
            View todayIndicator = dayView.findViewById(R.id.today_indicator);
            
            if (i < offset || dayCounter > daysInMonth) {
                dayView.setVisibility(View.INVISIBLE);
            } else {
                final int day = dayCounter;
                tvDayNumber.setText(String.valueOf(day));
                
                cal.set(Calendar.DAY_OF_MONTH, day);
                boolean isToday = DateUtils.isToday(cal);
                boolean isFutureDay = DateUtils.isFuture(cal);
                
                if (isToday) {
                    tvDayNumber.setTextColor(getResources().getColor(R.color.very_good, null));
                    todayIndicator.setVisibility(View.VISIBLE);
                }
                
                String dateKey = DateUtils.getDateKey(cal);
                
                if (entriesByDate.containsKey(dateKey) && !entriesByDate.get(dateKey).isEmpty()) {
                    List<JournalEntryEntity> dayEntries = entriesByDate.get(dateKey);
                    int mood = dayEntries.get(0).getMoodLevel();
                    
                    btnAddEntry.setVisibility(View.GONE);
                    imgEmoji.setVisibility(View.VISIBLE);
                    imgEmoji.setImageResource(MoodUtils.getIcon(mood));
                    imgEmoji.setOnClickListener(v -> showSelectedDayInfo(day, dateKey, dayEntries));
                } else if (isFutureDay) {
                    btnAddEntry.setVisibility(View.VISIBLE);
                    btnAddEntry.setAlpha(0.15f);
                    imgEmoji.setVisibility(View.GONE);
                    btnAddEntry.setOnClickListener(null);
                    tvDayNumber.setAlpha(0.4f);
                } else {
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
    
    private void showSelectedDayInfo(int day, String dateKey, List<JournalEntryEntity> entries) {
        selectedDay = day;
        selectedDayCard.setVisibility(View.VISIBLE);
        
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        selectedDayDate.setText(DateUtils.format(cal, DateUtils.PATTERN_FULL_DATE));
        
        int mood = entries.get(0).getMoodLevel();
        int entryCount = entries.size();
        
        selectedDayEmoji.setImageResource(MoodUtils.getIcon(mood));
        selectedDayMood.setText(entryCount > 1 ? entryCount + " entries" : MoodUtils.getText(mood));
        
        btnViewEntry.setOnClickListener(v -> openDayEntries(day, dateKey));
    }
    
    private void openDayEntries(int day, String dateKey) {
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        
        Intent intent = new Intent(this, AllEntriesActivity.class);
        intent.putExtra(AllEntriesActivity.EXTRA_FILTER_DATE, dateKey);
        intent.putExtra(AllEntriesActivity.EXTRA_FILTER_DATE_MILLIS_START, DateUtils.getStartOfDay(cal));
        intent.putExtra(AllEntriesActivity.EXTRA_FILTER_DATE_MILLIS_END, DateUtils.getEndOfDay(cal));
        startActivity(intent);
    }
    
    private void openAddEntry(int day) {
        Intent intent = new Intent(this, MoodSelectionActivity.class);
        intent.putExtra(MoodSelectionActivity.EXTRA_DAY, day);
        intent.putExtra(MoodSelectionActivity.EXTRA_MONTH, currentCalendar.get(Calendar.MONTH));
        intent.putExtra(MoodSelectionActivity.EXTRA_YEAR, currentCalendar.get(Calendar.YEAR));
        startActivity(intent);
    }
}
