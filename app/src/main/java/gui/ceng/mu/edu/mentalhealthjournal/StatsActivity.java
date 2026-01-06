package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity displaying comprehensive statistics with charts.
 * Shows weekly, monthly, and yearly statistics for mood trends and media attachments.
 */
public class StatsActivity extends AppCompatActivity {

    private static final int PERIOD_WEEKLY = 0;
    private static final int PERIOD_MONTHLY = 1;
    private static final int PERIOD_YEARLY = 2;

    private JournalRepository repository;
    private Handler mainHandler;
    private int currentPeriod = PERIOD_WEEKLY;

    // Views
    private TextView tabWeekly, tabMonthly, tabYearly;
    private TextView statTotalEntries, statAvgMood, statStreak;
    private TextView statPhotos, statVoiceMemos;
    private LineChart moodLineChart;
    private PieChart moodPieChart;
    private BarChart mediaBarChart, entriesBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // Check for period parameter from intent
        String period = getIntent().getStringExtra("period");
        if ("weekly".equals(period)) {
            currentPeriod = PERIOD_WEEKLY;
        } else if ("monthly".equals(period)) {
            currentPeriod = PERIOD_MONTHLY;
        } else if ("yearly".equals(period)) {
            currentPeriod = PERIOD_YEARLY;
        }

        initViews();
        setupBottomNavigation();
        setupTabListeners();
        
        // Update tab selection based on period
        updateTabSelection();
        
        loadStatistics();
    }

    private void updateTabSelection() {
        // Reset all tabs
        tabWeekly.setBackgroundResource(R.drawable.tab_unselected_background);
        tabMonthly.setBackgroundResource(R.drawable.tab_unselected_background);
        tabYearly.setBackgroundResource(R.drawable.tab_unselected_background);
        tabWeekly.setTextColor(0xFF888888);
        tabMonthly.setTextColor(0xFF888888);
        tabYearly.setTextColor(0xFF888888);
        
        // Select current tab
        switch (currentPeriod) {
            case PERIOD_WEEKLY:
                tabWeekly.setBackgroundResource(R.drawable.tab_selected_background);
                tabWeekly.setTextColor(0xFFFFFFFF);
                break;
            case PERIOD_MONTHLY:
                tabMonthly.setBackgroundResource(R.drawable.tab_selected_background);
                tabMonthly.setTextColor(0xFFFFFFFF);
                break;
            case PERIOD_YEARLY:
                tabYearly.setBackgroundResource(R.drawable.tab_selected_background);
                tabYearly.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    private void initViews() {
        tabWeekly = findViewById(R.id.tab_weekly);
        tabMonthly = findViewById(R.id.tab_monthly);
        tabYearly = findViewById(R.id.tab_yearly);

        statTotalEntries = findViewById(R.id.stat_total_entries);
        statAvgMood = findViewById(R.id.stat_avg_mood);
        statStreak = findViewById(R.id.stat_streak);
        statPhotos = findViewById(R.id.stat_photos);
        statVoiceMemos = findViewById(R.id.stat_voice_memos);

        moodLineChart = findViewById(R.id.mood_line_chart);
        moodPieChart = findViewById(R.id.mood_pie_chart);
        mediaBarChart = findViewById(R.id.media_bar_chart);
        entriesBarChart = findViewById(R.id.entries_bar_chart);

        setupCharts();
    }

    private void setupCharts() {
        // Line Chart setup
        moodLineChart.getDescription().setEnabled(false);
        moodLineChart.setTouchEnabled(true);
        moodLineChart.setDragEnabled(true);
        moodLineChart.setScaleEnabled(false);
        moodLineChart.setPinchZoom(false);
        moodLineChart.setDrawGridBackground(false);
        moodLineChart.getLegend().setEnabled(false);
        moodLineChart.getAxisRight().setEnabled(false);
        moodLineChart.setExtraBottomOffset(10f);

        XAxis xAxisLine = moodLineChart.getXAxis();
        xAxisLine.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisLine.setTextColor(Color.WHITE);
        xAxisLine.setDrawGridLines(false);
        xAxisLine.setGranularity(1f);

        YAxis yAxisLine = moodLineChart.getAxisLeft();
        yAxisLine.setTextColor(Color.WHITE);
        yAxisLine.setDrawGridLines(true);
        yAxisLine.setGridColor(Color.parseColor("#3A3A3A"));
        yAxisLine.setAxisMinimum(1f);
        yAxisLine.setAxisMaximum(5f);
        yAxisLine.setGranularity(1f);

        // Pie Chart setup
        moodPieChart.getDescription().setEnabled(false);
        moodPieChart.setUsePercentValues(true);
        moodPieChart.setDrawHoleEnabled(true);
        moodPieChart.setHoleColor(Color.parseColor("#2C2C2C"));
        moodPieChart.setHoleRadius(45f);
        moodPieChart.setTransparentCircleRadius(50f);
        moodPieChart.setDrawEntryLabels(false);
        moodPieChart.setRotationEnabled(true);
        moodPieChart.setHighlightPerTapEnabled(true);

        Legend legendPie = moodPieChart.getLegend();
        legendPie.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legendPie.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legendPie.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legendPie.setDrawInside(false);
        legendPie.setTextColor(Color.WHITE);
        legendPie.setTextSize(10f);

        // Media Bar Chart setup
        setupBarChart(mediaBarChart);

        // Entries Bar Chart setup
        setupBarChart(entriesBarChart);
    }

    private void setupBarChart(BarChart barChart) {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setScaleEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setExtraBottomOffset(10f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setTextColor(Color.WHITE);
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#3A3A3A"));
        yAxis.setAxisMinimum(0f);
        yAxis.setGranularity(1f);
    }

    private void setupTabListeners() {
        tabWeekly.setOnClickListener(v -> selectPeriod(PERIOD_WEEKLY));
        tabMonthly.setOnClickListener(v -> selectPeriod(PERIOD_MONTHLY));
        tabYearly.setOnClickListener(v -> selectPeriod(PERIOD_YEARLY));
    }

    private void selectPeriod(int period) {
        currentPeriod = period;
        updateTabUI();
        loadStatistics();
    }

    private void updateTabUI() {
        // Reset all tabs
        tabWeekly.setBackgroundResource(0);
        tabMonthly.setBackgroundResource(0);
        tabYearly.setBackgroundResource(0);
        tabWeekly.setTextColor(Color.parseColor("#888888"));
        tabMonthly.setTextColor(Color.parseColor("#888888"));
        tabYearly.setTextColor(Color.parseColor("#888888"));

        // Highlight selected tab
        TextView selectedTab;
        switch (currentPeriod) {
            case PERIOD_MONTHLY:
                selectedTab = tabMonthly;
                break;
            case PERIOD_YEARLY:
                selectedTab = tabYearly;
                break;
            default:
                selectedTab = tabWeekly;
                break;
        }
        selectedTab.setBackgroundResource(R.drawable.tab_selected_background);
        selectedTab.setTextColor(Color.WHITE);
    }

    private long[] getDateRange() {
        Calendar calendar = Calendar.getInstance();
        // Set end time to end of today
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTime = calendar.getTimeInMillis();

        // Calculate start time based on period
        switch (currentPeriod) {
            case PERIOD_MONTHLY:
                calendar.add(Calendar.MONTH, -1);
                break;
            case PERIOD_YEARLY:
                calendar.add(Calendar.YEAR, -1);
                break;
            default: // WEEKLY
                calendar.add(Calendar.DAY_OF_YEAR, -6); // -6 to include today = 7 days total
                break;
        }
        // Set start time to beginning of that day
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        return new long[]{startTime, endTime};
    }

    private void loadStatistics() {
        long[] range = getDateRange();
        long startTime = range[0];
        long endTime = range[1];

        // Load entries for the selected period
        repository.getEntriesInRangeSync(startTime, endTime, new JournalRepository.RepositoryCallback<List<JournalEntryEntity>>() {
            @Override
            public void onComplete(List<JournalEntryEntity> entries) {
                mainHandler.post(() -> {
                    updateOverviewStats(entries, startTime, endTime);
                    updateMoodLineChart(entries);
                    updateMoodPieChart(entries);
                    updateEntriesBarChart(entries);
                });
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });

        // Load media statistics
        loadMediaStats(startTime, endTime);
    }

    private void updateOverviewStats(List<JournalEntryEntity> entries, long startTime, long endTime) {
        // Total entries
        statTotalEntries.setText(String.valueOf(entries.size()));

        // Average mood
        if (!entries.isEmpty()) {
            float totalMood = 0;
            for (JournalEntryEntity entry : entries) {
                totalMood += entry.getMoodLevel();
            }
            float avgMood = totalMood / entries.size();
            statAvgMood.setText(String.format(Locale.US, "%.1f", avgMood));
        } else {
            statAvgMood.setText("--");
        }

        // Calculate streak
        calculateStreak(entries);
    }

    private void calculateStreak(List<JournalEntryEntity> allEntries) {
        if (allEntries.isEmpty()) {
            statStreak.setText("0");
            return;
        }

        // Get all entries to calculate proper streak
        repository.getEntriesInRangeSync(0, System.currentTimeMillis(), new JournalRepository.RepositoryCallback<List<JournalEntryEntity>>() {
            @Override
            public void onComplete(List<JournalEntryEntity> entries) {
                mainHandler.post(() -> {
                    int streak = calculateConsecutiveDays(entries);
                    statStreak.setText(String.valueOf(streak));
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> statStreak.setText("0"));
            }
        });
    }

    private int calculateConsecutiveDays(List<JournalEntryEntity> entries) {
        if (entries.isEmpty()) return 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Map<String, Boolean> datesWithEntries = new HashMap<>();

        for (JournalEntryEntity entry : entries) {
            String dateKey = dateFormat.format(new Date(entry.getTimestamp()));
            datesWithEntries.put(dateKey, true);
        }

        Calendar calendar = Calendar.getInstance();
        String today = dateFormat.format(calendar.getTime());

        // Check if there's an entry today
        if (!datesWithEntries.containsKey(today)) {
            // Check yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            today = dateFormat.format(calendar.getTime());
            if (!datesWithEntries.containsKey(today)) {
                return 0;
            }
        }

        int streak = 0;
        calendar = Calendar.getInstance();

        while (true) {
            String dateKey = dateFormat.format(calendar.getTime());
            if (datesWithEntries.containsKey(dateKey)) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    private void updateMoodLineChart(List<JournalEntryEntity> entries) {
        if (entries.isEmpty()) {
            moodLineChart.clear();
            moodLineChart.invalidate();
            return;
        }

        ArrayList<Entry> lineEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        SimpleDateFormat dateFormat;
        switch (currentPeriod) {
            case PERIOD_YEARLY:
                dateFormat = new SimpleDateFormat("MMM", Locale.US);
                break;
            case PERIOD_MONTHLY:
                dateFormat = new SimpleDateFormat("dd", Locale.US);
                break;
            default:
                dateFormat = new SimpleDateFormat("EEE", Locale.US);
                break;
        }

        // Group entries by date and calculate average mood per day
        Map<String, List<Integer>> moodsByDate = new HashMap<>();
        for (JournalEntryEntity entry : entries) {
            String dateKey = dateFormat.format(new Date(entry.getTimestamp()));
            if (!moodsByDate.containsKey(dateKey)) {
                moodsByDate.put(dateKey, new ArrayList<>());
            }
            moodsByDate.get(dateKey).add(entry.getMoodLevel());
        }

        int index = 0;
        for (JournalEntryEntity entry : entries) {
            String dateKey = dateFormat.format(new Date(entry.getTimestamp()));
            if (!labels.contains(dateKey)) {
                labels.add(dateKey);
                List<Integer> moods = moodsByDate.get(dateKey);
                float avgMood = 0;
                for (int mood : moods) avgMood += mood;
                avgMood /= moods.size();
                lineEntries.add(new Entry(index++, avgMood));
            }
        }

        LineDataSet dataSet = new LineDataSet(lineEntries, "Mood");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setCircleColor(Color.parseColor("#4CAF50"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4CAF50"));
        dataSet.setFillAlpha(50);

        LineData lineData = new LineData(dataSet);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.1f", value);
            }
        });

        moodLineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        moodLineChart.setData(lineData);
        moodLineChart.animateX(500);
        moodLineChart.invalidate();
    }

    private void updateMoodPieChart(List<JournalEntryEntity> entries) {
        if (entries.isEmpty()) {
            moodPieChart.clear();
            moodPieChart.invalidate();
            return;
        }

        int[] moodCounts = new int[5]; // Index 0-4 for mood levels 1-5
        for (JournalEntryEntity entry : entries) {
            int moodLevel = entry.getMoodLevel();
            if (moodLevel >= 1 && moodLevel <= 5) {
                moodCounts[moodLevel - 1]++;
            }
        }

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String[] moodNames = {"Very Bad", "Bad", "Normal", "Good", "Very Good"};
        int[] moodColors = {
                Color.parseColor("#F44336"), // Very Bad - Red
                Color.parseColor("#FF9800"), // Bad - Orange
                Color.parseColor("#FFEB3B"), // Normal - Yellow
                Color.parseColor("#8BC34A"), // Good - Light Green
                Color.parseColor("#4CAF50")  // Very Good - Green
        };

        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (moodCounts[i] > 0) {
                pieEntries.add(new PieEntry(moodCounts[i], moodNames[i]));
                colors.add(moodColors[i]);
            }
        }

        if (pieEntries.isEmpty()) {
            moodPieChart.clear();
            moodPieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f%%", value);
            }
        });

        moodPieChart.setData(pieData);
        moodPieChart.animateY(500);
        moodPieChart.invalidate();
    }

    private void loadMediaStats(long startTime, long endTime) {
        // Photos count
        repository.getPhotoCountInRange(startTime, endTime, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> statPhotos.setText(String.valueOf(count)));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> statPhotos.setText("0"));
            }
        });

        // Voice memos count
        repository.getVoiceMemoCountInRange(startTime, endTime, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> statVoiceMemos.setText(String.valueOf(count)));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> statVoiceMemos.setText("0"));
            }
        });

        // Media bar chart
        updateMediaBarChart(startTime, endTime);
    }

    private void updateMediaBarChart(long startTime, long endTime) {
        final int[] photoCount = {0};
        final int[] voiceCount = {0};
        final int[] callbacksComplete = {0};

        repository.getPhotoCountInRange(startTime, endTime, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                photoCount[0] = count;
                callbacksComplete[0]++;
                if (callbacksComplete[0] == 2) {
                    mainHandler.post(() -> renderMediaBarChart(photoCount[0], voiceCount[0]));
                }
            }

            @Override
            public void onError(Exception e) {
                callbacksComplete[0]++;
            }
        });

        repository.getVoiceMemoCountInRange(startTime, endTime, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                voiceCount[0] = count;
                callbacksComplete[0]++;
                if (callbacksComplete[0] == 2) {
                    mainHandler.post(() -> renderMediaBarChart(photoCount[0], voiceCount[0]));
                }
            }

            @Override
            public void onError(Exception e) {
                callbacksComplete[0]++;
            }
        });
    }

    private void renderMediaBarChart(int photos, int voiceMemos) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, photos));
        barEntries.add(new BarEntry(1, voiceMemos));

        BarDataSet dataSet = new BarDataSet(barEntries, "Media");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#2196F3")); // Photos - Blue
        colors.add(Color.parseColor("#E91E63")); // Voice Memos - Pink
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        String[] labels = {"Photos", "Voice Memos"};
        mediaBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        mediaBarChart.setData(barData);
        mediaBarChart.animateY(500);
        mediaBarChart.invalidate();
    }

    private void updateEntriesBarChart(List<JournalEntryEntity> entries) {
        if (entries.isEmpty()) {
            entriesBarChart.clear();
            entriesBarChart.invalidate();
            return;
        }

        SimpleDateFormat dateFormat;
        int numBars;

        switch (currentPeriod) {
            case PERIOD_YEARLY:
                dateFormat = new SimpleDateFormat("MMM", Locale.US);
                numBars = 12;
                break;
            case PERIOD_MONTHLY:
                dateFormat = new SimpleDateFormat("W", Locale.US);
                numBars = 4;
                break;
            default:
                dateFormat = new SimpleDateFormat("EEE", Locale.US);
                numBars = 7;
                break;
        }

        // Count entries per time period
        Map<String, Integer> entriesByPeriod = new HashMap<>();
        for (JournalEntryEntity entry : entries) {
            String key = dateFormat.format(new Date(entry.getTimestamp()));
            entriesByPeriod.put(key, entriesByPeriod.getOrDefault(key, 0) + 1);
        }

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> mapEntry : entriesByPeriod.entrySet()) {
            barEntries.add(new BarEntry(index, mapEntry.getValue()));
            labels.add(mapEntry.getKey());
            index++;
        }

        if (barEntries.isEmpty()) {
            entriesBarChart.clear();
            entriesBarChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(barEntries, "Entries");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        entriesBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        entriesBarChart.setData(barData);
        entriesBarChart.animateY(500);
        entriesBarChart.invalidate();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_stats);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_calendar) {
                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_stats) {
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
        // Refresh the bottom navigation selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_stats);
        // Reload statistics
        loadStatistics();
    }
}
