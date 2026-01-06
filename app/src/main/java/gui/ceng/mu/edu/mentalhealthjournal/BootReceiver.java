package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * BroadcastReceiver that handles device boot.
 * Re-schedules daily reminder alarms after device restart.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_DAILY_REMINDER = "daily_reminder";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            rescheduleReminders(context);
        }
    }

    private void rescheduleReminders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        boolean dailyReminderEnabled = prefs.getBoolean(KEY_DAILY_REMINDER, true);
        
        if (dailyReminderEnabled) {
            int hour = prefs.getInt(KEY_REMINDER_HOUR, 20);
            int minute = prefs.getInt(KEY_REMINDER_MINUTE, 0);
            
            ReminderReceiver.scheduleReminder(context, hour, minute);
        }
    }
}
