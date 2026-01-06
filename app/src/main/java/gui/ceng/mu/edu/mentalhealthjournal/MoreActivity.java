package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;
import gui.ceng.mu.edu.mentalhealthjournal.util.BackupManager;
import gui.ceng.mu.edu.mentalhealthjournal.util.DateUtils;

public class MoreActivity extends BaseNavigationActivity {

    private JournalRepository repository;
    private BackupManager backupManager;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView greetingUser, summaryText, weeklyEntryCount, monthlyEntryCount, yearlyEntryCount, photoCount, voiceMemoCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        repository = new JournalRepository(this);
        backupManager = new BackupManager(this);
        initViews();
        setupBottomNavigation(R.id.navigation_more);
        setupMenuClickListeners();
        loadStatisticsFromDatabase();
    }

    @Override protected int getCurrentNavigationItem() { return R.id.navigation_more; }

    private void initViews() {
        greetingUser = findViewById(R.id.greeting_user);
        summaryText = findViewById(R.id.summary_text);
        weeklyEntryCount = findViewById(R.id.weekly_entry_count);
        monthlyEntryCount = findViewById(R.id.monthly_entry_count);
        yearlyEntryCount = findViewById(R.id.yearly_entry_count);
        photoCount = findViewById(R.id.photo_count);
        voiceMemoCount = findViewById(R.id.voice_memo_count);
        greetingUser.setText("Hi, " + getSharedPreferences("app_prefs", MODE_PRIVATE).getString("user_name", "User"));
    }

    private void setupMenuClickListeners() {
        findViewById(R.id.menu_weekly_reports).setOnClickListener(v -> openStats("weekly"));
        findViewById(R.id.menu_monthly_reports).setOnClickListener(v -> openStats("monthly"));
        findViewById(R.id.menu_yearly_report).setOnClickListener(v -> openStats("yearly"));
        findViewById(R.id.menu_photo_gallery).setOnClickListener(v -> startActivity(new Intent(this, PhotoGalleryActivity.class)));
        findViewById(R.id.menu_voice_memos).setOnClickListener(v -> startActivity(new Intent(this, VoiceMemosActivity.class)));
        findViewById(R.id.menu_settings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.menu_backup).setOnClickListener(v -> showBackupRestoreDialog());
        findViewById(R.id.menu_help).setOnClickListener(v -> startActivity(new Intent(this, HelpActivity.class)));
        findViewById(R.id.menu_feedback).setOnClickListener(v -> sendFeedback());
        findViewById(R.id.menu_about).setOnClickListener(v -> showAbout());
    }

    private void openStats(String period) { startActivity(new Intent(this, StatsActivity.class).putExtra("period", period)); overridePendingTransition(0, 0); }

    private void showBackupRestoreDialog() {
        new MaterialAlertDialogBuilder(this).setTitle("Backup & Restore")
            .setItems(new String[]{"Backup Data", "Restore Data"}, (d, w) -> { if (w == 0) performBackup(); else performRestore(); })
            .setNegativeButton("Cancel", null).show();
    }

    private void performBackup() {
        backupManager.createBackup(new BackupManager.BackupCallback() {
            @Override public void onSuccess(String m) { mainHandler.post(() -> Toast.makeText(MoreActivity.this, R.string.backup_success, Toast.LENGTH_SHORT).show()); }
            @Override public void onError(String e) { mainHandler.post(() -> Toast.makeText(MoreActivity.this, getString(R.string.backup_failed) + ": " + e, Toast.LENGTH_SHORT).show()); }
        });
    }

    private void performRestore() {
        if (!backupManager.hasBackup()) { Toast.makeText(this, R.string.no_backup_found, Toast.LENGTH_SHORT).show(); return; }
        new MaterialAlertDialogBuilder(this).setTitle(R.string.confirm_restore).setMessage(R.string.restore_warning)
            .setPositiveButton(R.string.restore, (d, w) -> backupManager.restoreFromBackup(new BackupManager.BackupCallback() {
                @Override public void onSuccess(String m) { mainHandler.post(() -> Toast.makeText(MoreActivity.this, R.string.restore_success, Toast.LENGTH_SHORT).show()); }
                @Override public void onError(String e) { mainHandler.post(() -> Toast.makeText(MoreActivity.this, getString(R.string.restore_failed) + ": " + e, Toast.LENGTH_SHORT).show()); }
            })).setNegativeButton(R.string.cancel, null).show();
    }

    private void sendFeedback() {
        Intent i = new Intent(Intent.ACTION_SEND).setType("message/rfc822")
            .putExtra(Intent.EXTRA_EMAIL, new String[]{"support@mentalhealthjournal.app"})
            .putExtra(Intent.EXTRA_SUBJECT, "Mental Health Journal Feedback");
        try { startActivity(Intent.createChooser(i, "Send feedback...")); }
        catch (Exception e) { Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show(); }
    }

    private void showAbout() {
        new android.app.AlertDialog.Builder(this).setTitle("Mental Health Journal")
            .setMessage("Version 1.0\n\nTrack your moods, emotions, and daily activities to improve your mental well-being.\n\n© 2026 Mental Health Journal")
            .setPositiveButton("OK", null).show();
    }

    private void loadStatisticsFromDatabase() {
        long now = System.currentTimeMillis();
        long[] weekly = DateUtils.getDateRangeForPeriod(0), monthly = DateUtils.getDateRangeForPeriod(1);
        
        loadCount(weekly[0], now, weeklyEntryCount, "entries");
        loadCount(monthly[0], now, monthlyEntryCount, "entries");
        loadCount(DateUtils.getStartOfDay(DateUtils.getCalendar(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), 0, 1).getTimeInMillis()), now, yearlyEntryCount, "entries");
        
        repository.getTotalPhotoCount(cb(photoCount, "photos"));
        repository.getTotalVoiceMemoCount(cb(voiceMemoCount, "memos"));
        repository.getEntryCount(new JournalRepository.RepositoryCallback<Integer>() {
            @Override public void onComplete(Integer c) { mainHandler.post(() -> summaryText.setText(c == 0 ? "Start tracking your mental health journey" : c + " entries recorded. " + (c == 1 ? "Keep it up!" : "Great progress!"))); }
            @Override public void onError(Exception e) { mainHandler.post(() -> summaryText.setText("Track your mental health journey")); }
        });
    }

    private void loadCount(long start, long end, TextView tv, String suffix) {
        repository.getEntryCountInRange(start, end, cb(tv, suffix));
    }

    private JournalRepository.RepositoryCallback<Integer> cb(TextView tv, String suffix) {
        return new JournalRepository.RepositoryCallback<Integer>() {
            @Override public void onComplete(Integer c) { mainHandler.post(() -> tv.setText(c + " " + suffix)); }
            @Override public void onError(Exception e) { mainHandler.post(() -> tv.setText("0 " + suffix)); }
        };
    }

    @Override protected void onResume() {
        super.onResume();
        ((BottomNavigationView) findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.navigation_more);
        loadStatisticsFromDatabase();
    }
}

