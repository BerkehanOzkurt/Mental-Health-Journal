package gui.ceng.mu.edu.mentalhealthjournal;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

import java.util.Locale;

/**
 * Settings Activity for app preferences and configuration.
 * Manages themes, notifications, privacy, and data management settings.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_THEME = "theme";
    private static final String KEY_START_WEEK = "start_week";
    private static final String KEY_DAILY_REMINDER = "daily_reminder";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    private static final String KEY_PIN_ENABLED = "pin_enabled";

    private SharedPreferences prefs;
    private JournalRepository repository;

    // Views
    private TextView themeValue;
    private TextView startWeekValue;
    private SwitchCompat switchDailyReminder;
    private TextView reminderTimeValue;
    private SwitchCompat switchPinLock;
    private TextView nameValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        repository = new JournalRepository(this);

        initViews();
        loadSettings();
        setupClickListeners();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        themeValue = findViewById(R.id.theme_value);
        startWeekValue = findViewById(R.id.start_week_value);
        switchDailyReminder = findViewById(R.id.switch_daily_reminder);
        reminderTimeValue = findViewById(R.id.reminder_time_value);
        switchPinLock = findViewById(R.id.switch_pin_lock);
        nameValue = findViewById(R.id.name_value);
    }

    private void loadSettings() {
        // Theme
        String theme = prefs.getString(KEY_THEME, "Dark");
        themeValue.setText(theme);

        // Start of week
        String startWeek = prefs.getString(KEY_START_WEEK, "Monday");
        startWeekValue.setText(startWeek);

        // Daily reminder
        boolean dailyReminder = prefs.getBoolean(KEY_DAILY_REMINDER, true);
        switchDailyReminder.setChecked(dailyReminder);

        // Reminder time
        int hour = prefs.getInt(KEY_REMINDER_HOUR, 20);
        int minute = prefs.getInt(KEY_REMINDER_MINUTE, 0);
        reminderTimeValue.setText(String.format(Locale.US, "%02d:%02d", hour, minute));

        // PIN lock
        boolean pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false);
        switchPinLock.setChecked(pinEnabled);

        // User name
        String userName = prefs.getString(KEY_USER_NAME, "User");
        nameValue.setText(userName);
    }

    private void setupClickListeners() {
        // Theme selector
        findViewById(R.id.setting_theme).setOnClickListener(v -> showThemeDialog());

        // Start of week selector
        findViewById(R.id.setting_start_week).setOnClickListener(v -> showStartWeekDialog());

        // Daily reminder toggle
        switchDailyReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DAILY_REMINDER, isChecked).apply();
            showToast(isChecked ? "Daily reminder enabled" : "Daily reminder disabled");
        });

        // Reminder time picker
        findViewById(R.id.setting_reminder_time).setOnClickListener(v -> showTimePickerDialog());

        // PIN lock toggle
        switchPinLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_PIN_ENABLED, isChecked).apply();
            if (isChecked) {
                // Launch PIN setup
                Intent intent = new Intent(this, PinActivity.class);
                intent.putExtra("setup_mode", true);
                startActivity(intent);
            }
            showToast(isChecked ? "PIN lock enabled" : "PIN lock disabled");
        });

        // Change PIN
        findViewById(R.id.setting_change_pin).setOnClickListener(v -> {
            Intent intent = new Intent(this, PinActivity.class);
            intent.putExtra("change_pin", true);
            startActivity(intent);
        });

        // Export data
        findViewById(R.id.setting_export_data).setOnClickListener(v -> 
            showToast("Export feature coming soon!"));

        // Import data
        findViewById(R.id.setting_import_data).setOnClickListener(v -> 
            showToast("Import feature coming soon!"));

        // Clear all data
        findViewById(R.id.setting_clear_data).setOnClickListener(v -> showClearDataDialog());

        // Change name
        findViewById(R.id.setting_name).setOnClickListener(v -> showNameDialog());
    }

    private void showThemeDialog() {
        String[] themes = {"Dark", "Light", "System Default"};
        int currentIndex = 0;
        String currentTheme = prefs.getString(KEY_THEME, "Dark");
        for (int i = 0; i < themes.length; i++) {
            if (themes[i].equals(currentTheme)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themes, currentIndex, (dialog, which) -> {
                    String selectedTheme = themes[which];
                    prefs.edit().putString(KEY_THEME, selectedTheme).apply();
                    themeValue.setText(selectedTheme);
                    showToast("Theme changed to " + selectedTheme);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showStartWeekDialog() {
        String[] days = {"Sunday", "Monday", "Saturday"};
        int currentIndex = 1;
        String currentDay = prefs.getString(KEY_START_WEEK, "Monday");
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(currentDay)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Start of Week")
                .setSingleChoiceItems(days, currentIndex, (dialog, which) -> {
                    String selectedDay = days[which];
                    prefs.edit().putString(KEY_START_WEEK, selectedDay).apply();
                    startWeekValue.setText(selectedDay);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimePickerDialog() {
        int hour = prefs.getInt(KEY_REMINDER_HOUR, 20);
        int minute = prefs.getInt(KEY_REMINDER_MINUTE, 0);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    prefs.edit()
                            .putInt(KEY_REMINDER_HOUR, hourOfDay)
                            .putInt(KEY_REMINDER_MINUTE, minuteOfHour)
                            .apply();
                    reminderTimeValue.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour));
                    showToast("Reminder set for " + String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour));
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete all journal entries? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    repository.deleteAllEntries();
                    showToast("All entries deleted");
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showNameDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(prefs.getString(KEY_USER_NAME, "User"));
        input.setTextColor(getResources().getColor(android.R.color.black));
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Your Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        prefs.edit().putString(KEY_USER_NAME, name).apply();
                        nameValue.setText(name);
                        showToast("Name updated");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
