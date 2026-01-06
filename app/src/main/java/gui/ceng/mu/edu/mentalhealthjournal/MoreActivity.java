package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

import java.util.Calendar;

/**
 * Activity displaying more options, settings, and statistics summaries.
 * Connects to database to show entry counts and media statistics.
 */
public class MoreActivity extends AppCompatActivity {

    private JournalRepository repository;
    private Handler mainHandler;

    // Views for displaying counts
    private TextView greetingUser;
    private TextView summaryText;
    private TextView weeklyEntryCount;
    private TextView monthlyEntryCount;
    private TextView yearlyEntryCount;
    private TextView photoCount;
    private TextView voiceMemoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupBottomNavigation();
        setupMenuClickListeners();
        loadStatisticsFromDatabase();
        updateGreeting();
    }

    private void initViews() {
        greetingUser = findViewById(R.id.greeting_user);
        summaryText = findViewById(R.id.summary_text);
        weeklyEntryCount = findViewById(R.id.weekly_entry_count);
        monthlyEntryCount = findViewById(R.id.monthly_entry_count);
        yearlyEntryCount = findViewById(R.id.yearly_entry_count);
        photoCount = findViewById(R.id.photo_count);
        voiceMemoCount = findViewById(R.id.voice_memo_count);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_more);

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
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.navigation_more) {
                return true;
            }
            return false;
        });
    }

    private void setupMenuClickListeners() {
        // Reports
        findViewById(R.id.menu_weekly_reports).setOnClickListener(v -> openStatsWithPeriod("weekly"));
        findViewById(R.id.menu_monthly_reports).setOnClickListener(v -> openStatsWithPeriod("monthly"));
        findViewById(R.id.menu_yearly_report).setOnClickListener(v -> openStatsWithPeriod("yearly"));

        // Media & Memories
        findViewById(R.id.menu_photo_gallery).setOnClickListener(v -> 
            startActivity(new Intent(this, PhotoGalleryActivity.class)));
        findViewById(R.id.menu_voice_memos).setOnClickListener(v -> 
            startActivity(new Intent(this, VoiceMemosActivity.class)));

        // Settings
        findViewById(R.id.menu_settings).setOnClickListener(v -> 
            startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.menu_edit_moods).setOnClickListener(v -> 
            startActivity(new Intent(this, EditMoodsActivity.class)));
        findViewById(R.id.menu_edit_activities).setOnClickListener(v -> 
            startActivity(new Intent(this, EditActivitiesActivity.class)));
        findViewById(R.id.menu_backup).setOnClickListener(v -> 
            showBackupRestoreDialog());

        // Support
        findViewById(R.id.menu_help).setOnClickListener(v -> 
            startActivity(new Intent(this, HelpActivity.class)));
        findViewById(R.id.menu_feedback).setOnClickListener(v -> 
            sendFeedbackEmail());
        findViewById(R.id.menu_about).setOnClickListener(v -> 
            showAboutDialog());
    }

    private void showBackupRestoreDialog() {
        String[] options = {"Backup Data", "Restore Data"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Backup & Restore")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showToast("Backup feature coming soon!");
                    } else {
                        showToast("Restore feature coming soon!");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendFeedbackEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@mentalhealthjournal.app"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mental Health Journal Feedback");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send feedback..."));
        } catch (android.content.ActivityNotFoundException ex) {
            showToast("No email app installed");
        }
    }

    private void openStatsWithPeriod(String period) {
        Intent intent = new Intent(this, StatsActivity.class);
        intent.putExtra("period", period);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void loadStatisticsFromDatabase() {
        Calendar calendar = Calendar.getInstance();
        // Set end time to end of today
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long now = calendar.getTimeInMillis();

        // Weekly (last 7 days including today)
        Calendar weekCal = Calendar.getInstance();
        weekCal.add(Calendar.DAY_OF_YEAR, -6); // -6 + today = 7 days
        weekCal.set(Calendar.HOUR_OF_DAY, 0);
        weekCal.set(Calendar.MINUTE, 0);
        weekCal.set(Calendar.SECOND, 0);
        weekCal.set(Calendar.MILLISECOND, 0);
        long weekStart = weekCal.getTimeInMillis();

        repository.getEntryCountInRange(weekStart, now, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> weeklyEntryCount.setText(count + " entries"));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> weeklyEntryCount.setText("0 entries"));
            }
        });

        // Monthly (last 30 days including today)
        Calendar monthCal = Calendar.getInstance();
        monthCal.add(Calendar.MONTH, -1);
        monthCal.set(Calendar.HOUR_OF_DAY, 0);
        monthCal.set(Calendar.MINUTE, 0);
        monthCal.set(Calendar.SECOND, 0);
        monthCal.set(Calendar.MILLISECOND, 0);
        long monthStart = monthCal.getTimeInMillis();

        repository.getEntryCountInRange(monthStart, now, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> monthlyEntryCount.setText(count + " entries"));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> monthlyEntryCount.setText("0 entries"));
            }
        });

        // Yearly
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long yearStart = calendar.getTimeInMillis();

        repository.getEntryCountInRange(yearStart, now, new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> yearlyEntryCount.setText(count + " entries"));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> yearlyEntryCount.setText("0 entries"));
            }
        });

        // Photos
        repository.getTotalPhotoCount(new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> photoCount.setText(count + " photos"));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> photoCount.setText("0 photos"));
            }
        });

        // Voice memos
        repository.getTotalVoiceMemoCount(new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> voiceMemoCount.setText(count + " memos"));
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> voiceMemoCount.setText("0 memos"));
            }
        });

        // Update summary text with total entries
        repository.getEntryCount(new JournalRepository.RepositoryCallback<Integer>() {
            @Override
            public void onComplete(Integer count) {
                mainHandler.post(() -> {
                    if (count == 0) {
                        summaryText.setText("Start tracking your mental health journey");
                    } else if (count == 1) {
                        summaryText.setText("1 entry recorded. Keep it up!");
                    } else {
                        summaryText.setText(count + " entries recorded. Great progress!");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> summaryText.setText("Track your mental health journey"));
            }
        });
    }

    private void updateGreeting() {
        // You could load username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "User");
        greetingUser.setText("Hi, " + userName);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showAboutDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Mental Health Journal")
                .setMessage("Version 1.0\n\nTrack your moods, emotions, and daily activities to improve your mental well-being.\n\n© 2026 Mental Health Journal")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the bottom navigation selection
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_more);
        // Reload statistics
        loadStatisticsFromDatabase();
    }
}

