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
import gui.ceng.mu.edu.mentalhealthjournal.model.Mood;

/**
 * Repository for managing moods/emotions (default + custom).
 * Follows Repository pattern for clean data access.
 */
public class MoodRepository {

    private static final String PREFS_NAME = "mood_prefs";
    private static final String KEY_CUSTOM_MOODS = "custom_moods";
    private static final String KEY_MOOD_ORDER = "mood_order";

    /**
     * Default moods provided by the app.
     * These cannot be deleted by the user.
     */
    private static final List<Mood> DEFAULT_MOODS = Arrays.asList(
            new Mood("Happy", "😊", R.color.emotion_happy, true),
            new Mood("Sad", "😢", R.color.emotion_sad, true),
            new Mood("Anxious", "😰", R.color.emotion_anxious, true),
            new Mood("Calm", "😌", R.color.emotion_calm, true),
            new Mood("Angry", "😠", R.color.emotion_angry, true),
            new Mood("Grateful", "🙏", R.color.emotion_grateful, true),
            new Mood("Stressed", "😫", R.color.emotion_stressed, true),
            new Mood("Excited", "🎉", R.color.emotion_excited, true),
            new Mood("Lonely", "😔", R.color.emotion_lonely, true),
            new Mood("Hopeful", "🌟", R.color.emotion_hopeful, true),
            new Mood("Confused", "😕", R.color.emotion_confused, true),
            new Mood("Peaceful", "☮️", R.color.emotion_peaceful, true),
            new Mood("Overwhelmed", "😵", R.color.emotion_overwhelmed, true)
    );

    private final SharedPreferences prefs;
    private final Gson gson;

    public MoodRepository(@NonNull Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Returns all moods (defaults + user-created) in the current order.
     * @return Unmodifiable list of all moods
     */
    @NonNull
    public List<Mood> getAllMoods() {
        List<Mood> allMoods = new ArrayList<>(DEFAULT_MOODS);
        allMoods.addAll(getCustomMoods());
        return Collections.unmodifiableList(allMoods);
    }

    /**
     * Returns only the default moods.
     * @return Unmodifiable list of default moods
     */
    @NonNull
    public List<Mood> getDefaultMoods() {
        return Collections.unmodifiableList(DEFAULT_MOODS);
    }

    /**
     * Returns only user-created custom moods.
     * @return List of custom moods
     */
    @NonNull
    public List<Mood> getCustomMoods() {
        String json = prefs.getString(KEY_CUSTOM_MOODS, "[]");
        Type type = new TypeToken<List<CustomMoodData>>(){}.getType();
        List<CustomMoodData> customData = gson.fromJson(json, type);
        
        if (customData == null) {
            return new ArrayList<>();
        }
        
        return customData.stream()
                .map(data -> new Mood(data.name, data.emoji, R.color.emotion_custom, false))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new custom mood.
     * @param mood The mood to add
     * @throws IllegalArgumentException if mood name already exists
     */
    public void addCustomMood(@NonNull Mood mood) {
        if (moodExists(mood.getName())) {
            throw new IllegalArgumentException("Mood already exists: " + mood.getName());
        }

        List<CustomMoodData> customData = getCustomMoodData();
        customData.add(new CustomMoodData(mood.getName(), mood.getEmoji()));
        saveCustomMoods(customData);
    }

    /**
     * Updates an existing custom mood.
     * @param oldName The current name of the mood
     * @param newMood The updated mood data
     * @throws IllegalArgumentException if mood doesn't exist or is a default mood
     */
    public void updateCustomMood(@NonNull String oldName, @NonNull Mood newMood) {
        if (isDefaultMood(oldName)) {
            throw new IllegalArgumentException("Cannot update default mood: " + oldName);
        }

        List<CustomMoodData> customData = getCustomMoodData();
        for (int i = 0; i < customData.size(); i++) {
            if (customData.get(i).name.equalsIgnoreCase(oldName)) {
                customData.set(i, new CustomMoodData(newMood.getName(), newMood.getEmoji()));
                saveCustomMoods(customData);
                return;
            }
        }
        throw new IllegalArgumentException("Mood not found: " + oldName);
    }

    /**
     * Removes a custom mood.
     * @param moodName The name of the mood to remove
     * @throws IllegalArgumentException if mood is a default mood
     */
    public void removeCustomMood(@NonNull String moodName) {
        if (isDefaultMood(moodName)) {
            throw new IllegalArgumentException("Cannot delete default mood: " + moodName);
        }

        List<CustomMoodData> customData = getCustomMoodData().stream()
                .filter(data -> !data.name.equalsIgnoreCase(moodName))
                .collect(Collectors.toList());
        saveCustomMoods(customData);
    }

    /**
     * Checks if a mood with the given name exists.
     * @param name The mood name to check
     * @return true if mood exists
     */
    public boolean moodExists(@NonNull String name) {
        return getAllMoods().stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(name));
    }

    /**
     * Checks if a mood is a default (non-deletable) mood.
     * @param name The mood name to check
     * @return true if it's a default mood
     */
    public boolean isDefaultMood(@NonNull String name) {
        return DEFAULT_MOODS.stream()
                .anyMatch(m -> m.getName().equalsIgnoreCase(name));
    }

    /**
     * Gets a mood by name.
     * @param name The mood name
     * @return The mood, or null if not found
     */
    public Mood getMoodByName(@NonNull String name) {
        return getAllMoods().stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private List<CustomMoodData> getCustomMoodData() {
        String json = prefs.getString(KEY_CUSTOM_MOODS, "[]");
        Type type = new TypeToken<List<CustomMoodData>>(){}.getType();
        List<CustomMoodData> data = gson.fromJson(json, type);
        return data != null ? new ArrayList<>(data) : new ArrayList<>();
    }

    private void saveCustomMoods(List<CustomMoodData> moods) {
        prefs.edit()
                .putString(KEY_CUSTOM_MOODS, gson.toJson(moods))
                .apply();
    }

    /**
     * Internal data class for JSON serialization.
     */
    private static class CustomMoodData {
        String name;
        String emoji;

        CustomMoodData(String name, String emoji) {
            this.name = name;
            this.emoji = emoji;
        }
    }
}
