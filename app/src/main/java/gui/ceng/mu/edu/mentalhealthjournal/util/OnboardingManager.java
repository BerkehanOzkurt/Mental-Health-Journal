package gui.ceng.mu.edu.mentalhealthjournal.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Manages first-launch onboarding and user preferences.
 * Single Responsibility: Handle all onboarding-related logic.
 */
public class OnboardingManager {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private final SharedPreferences prefs;

    public OnboardingManager(@NonNull Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Checks if this is the first time the app has been launched.
     * @return true if first launch, false otherwise
     */
    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    /**
     * Checks if the onboarding flow has been completed.
     * @return true if onboarding is complete, false otherwise
     */
    public boolean isOnboardingComplete() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    /**
     * Marks onboarding as complete and saves the user's name.
     * @param userName The name entered by the user
     */
    public void completeOnboarding(@NonNull String userName) {
        String finalName = userName.trim();
        if (finalName.isEmpty()) {
            finalName = "Friend";
        }

        prefs.edit()
                .putBoolean(KEY_FIRST_LAUNCH, false)
                .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                .putString(KEY_USER_NAME, finalName)
                .apply();
    }

    /**
     * Gets the stored user name, defaulting to "Friend" if not set.
     * @return The user's name
     */
    @NonNull
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Friend");
    }

    /**
     * Updates the user's name.
     * @param userName The new name for the user
     */
    public void setUserName(@NonNull String userName) {
        String finalName = userName.trim();
        if (finalName.isEmpty()) {
            finalName = "Friend";
        }
        prefs.edit().putString(KEY_USER_NAME, finalName).apply();
    }

    /**
     * Resets onboarding state (useful for testing or settings reset).
     */
    public void resetOnboarding() {
        prefs.edit()
                .putBoolean(KEY_FIRST_LAUNCH, true)
                .putBoolean(KEY_ONBOARDING_COMPLETE, false)
                .remove(KEY_USER_NAME)
                .apply();
    }
}
