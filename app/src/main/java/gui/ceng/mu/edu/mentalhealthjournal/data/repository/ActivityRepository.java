package gui.ceng.mu.edu.mentalhealthjournal.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import gui.ceng.mu.edu.mentalhealthjournal.R;
import gui.ceng.mu.edu.mentalhealthjournal.model.Activity;

/**
 * Repository for managing activities (default + custom).
 * Follows Repository pattern for clean data access.
 */
public class ActivityRepository {

    private static final String PREFS_NAME = "activity_prefs";
    private static final String KEY_CUSTOM_ACTIVITIES = "custom_activities";

    /**
     * Default activities provided by the app.
     * These cannot be deleted by the user.
     */
    private static final List<Activity> DEFAULT_ACTIVITIES = Arrays.asList(
            new Activity("Exercise", "🏃", R.color.activity_custom, true),
            new Activity("Work", "💼", R.color.activity_custom, true),
            new Activity("Reading", "📖", R.color.activity_custom, true),
            new Activity("Music", "🎵", R.color.activity_custom, true),
            new Activity("Gaming", "🎮", R.color.activity_custom, true),
            new Activity("Cooking", "🍳", R.color.activity_custom, true),
            new Activity("Shopping", "🛒", R.color.activity_custom, true),
            new Activity("Socializing", "👥", R.color.activity_custom, true),
            new Activity("Meditation", "🧘", R.color.activity_custom, true),
            new Activity("Walking", "🚶", R.color.activity_custom, true),
            new Activity("Movies", "🎬", R.color.activity_custom, true),
            new Activity("Family", "👨‍👩‍👧‍👦", R.color.activity_custom, true),
            new Activity("Sports", "⚽", R.color.activity_custom, true),
            new Activity("Travel", "✈️", R.color.activity_custom, true),
            new Activity("Studying", "📚", R.color.activity_custom, true),
            new Activity("Art", "🎨", R.color.activity_custom, true),
            new Activity("Nature", "🌿", R.color.activity_custom, true),
            new Activity("Sleep", "😴", R.color.activity_custom, true)
    );

    private final SharedPreferences prefs;
    private final Gson gson;

    public ActivityRepository(@NonNull Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Returns all activities (defaults + user-created).
     * @return Unmodifiable list of all activities
     */
    @NonNull
    public List<Activity> getAllActivities() {
        List<Activity> allActivities = new ArrayList<>(DEFAULT_ACTIVITIES);
        allActivities.addAll(getCustomActivities());
        return Collections.unmodifiableList(allActivities);
    }

    /**
     * Returns only the default activities.
     * @return Unmodifiable list of default activities
     */
    @NonNull
    public List<Activity> getDefaultActivities() {
        return Collections.unmodifiableList(DEFAULT_ACTIVITIES);
    }

    /**
     * Returns only user-created custom activities.
     * @return List of custom activities
     */
    @NonNull
    public List<Activity> getCustomActivities() {
        String json = prefs.getString(KEY_CUSTOM_ACTIVITIES, "[]");
        Type type = new TypeToken<List<CustomActivityData>>(){}.getType();
        List<CustomActivityData> customData = gson.fromJson(json, type);

        if (customData == null) {
            return new ArrayList<>();
        }

        return customData.stream()
                .map(data -> new Activity(data.name, data.icon, R.color.activity_custom, false))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new custom activity.
     * @param activity The activity to add
     * @throws IllegalArgumentException if activity name already exists
     */
    public void addCustomActivity(@NonNull Activity activity) {
        if (activityExists(activity.getName())) {
            throw new IllegalArgumentException("Activity already exists: " + activity.getName());
        }

        List<CustomActivityData> customData = getCustomActivityData();
        customData.add(new CustomActivityData(activity.getName(), activity.getIcon()));
        saveCustomActivities(customData);
    }

    /**
     * Updates an existing custom activity.
     * @param oldName The current name of the activity
     * @param newActivity The updated activity data
     * @throws IllegalArgumentException if activity doesn't exist or is a default activity
     */
    public void updateCustomActivity(@NonNull String oldName, @NonNull Activity newActivity) {
        if (isDefaultActivity(oldName)) {
            throw new IllegalArgumentException("Cannot update default activity: " + oldName);
        }

        List<CustomActivityData> customData = getCustomActivityData();
        for (int i = 0; i < customData.size(); i++) {
            if (customData.get(i).name.equalsIgnoreCase(oldName)) {
                customData.set(i, new CustomActivityData(newActivity.getName(), newActivity.getIcon()));
                saveCustomActivities(customData);
                return;
            }
        }
        throw new IllegalArgumentException("Activity not found: " + oldName);
    }

    /**
     * Removes a custom activity.
     * @param activityName The name of the activity to remove
     * @throws IllegalArgumentException if activity is a default activity
     */
    public void removeCustomActivity(@NonNull String activityName) {
        if (isDefaultActivity(activityName)) {
            throw new IllegalArgumentException("Cannot delete default activity: " + activityName);
        }

        List<CustomActivityData> customData = getCustomActivityData().stream()
                .filter(data -> !data.name.equalsIgnoreCase(activityName))
                .collect(Collectors.toList());
        saveCustomActivities(customData);
    }

    /**
     * Checks if an activity with the given name exists.
     * @param name The activity name to check
     * @return true if activity exists
     */
    public boolean activityExists(@NonNull String name) {
        return getAllActivities().stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(name));
    }

    /**
     * Checks if an activity is a default (non-deletable) activity.
     * @param name The activity name to check
     * @return true if it's a default activity
     */
    public boolean isDefaultActivity(@NonNull String name) {
        return DEFAULT_ACTIVITIES.stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(name));
    }

    /**
     * Gets an activity by name.
     * @param name The activity name
     * @return The activity, or null if not found
     */
    public Activity getActivityByName(@NonNull String name) {
        return getAllActivities().stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private List<CustomActivityData> getCustomActivityData() {
        String json = prefs.getString(KEY_CUSTOM_ACTIVITIES, "[]");
        Type type = new TypeToken<List<CustomActivityData>>(){}.getType();
        List<CustomActivityData> data = gson.fromJson(json, type);
        return data != null ? new ArrayList<>(data) : new ArrayList<>();
    }

    private void saveCustomActivities(List<CustomActivityData> activities) {
        prefs.edit()
                .putString(KEY_CUSTOM_ACTIVITIES, gson.toJson(activities))
                .apply();
    }

    /**
     * Internal data class for JSON serialization.
     */
    private static class CustomActivityData {
        String name;
        String icon;

        CustomActivityData(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }
    }
}
