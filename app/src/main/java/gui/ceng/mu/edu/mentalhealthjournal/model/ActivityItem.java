package gui.ceng.mu.edu.mentalhealthjournal.model;

import gui.ceng.mu.edu.mentalhealthjournal.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a selectable activity/emotion item.
 * Used in the AddEntry screen for selecting emotions, sleep quality, and activities.
 */
public class ActivityItem {

    private String id;
    private String name;
    private int iconResourceId;
    private boolean isSelected;
    private String category; // "emotion", "sleep", "activity"

    public ActivityItem(String id, String name, int iconResourceId, String category) {
        this.id = id;
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.category = category;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public void setIconResourceId(int iconResourceId) {
        this.iconResourceId = iconResourceId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void toggleSelection() {
        this.isSelected = !this.isSelected;
    }

    /**
     * Get default list of emotions (as shown in the screenshots)
     */
    public static List<ActivityItem> getDefaultEmotions() {
        List<ActivityItem> emotions = new ArrayList<>();
        
        // Positive emotions
        emotions.add(new ActivityItem("happy", "happy", R.drawable.ic_emotion_happy, "emotion"));
        emotions.add(new ActivityItem("excited", "excited", R.drawable.ic_emotion_excited, "emotion"));
        emotions.add(new ActivityItem("grateful", "grateful", R.drawable.ic_emotion_grateful, "emotion"));
        emotions.add(new ActivityItem("relaxed", "relaxed", R.drawable.ic_emotion_relaxed, "emotion"));
        emotions.add(new ActivityItem("content", "content", R.drawable.ic_emotion_content, "emotion"));
        
        // Neutral/Negative emotions
        emotions.add(new ActivityItem("tired", "tired", R.drawable.ic_emotion_tired, "emotion"));
        emotions.add(new ActivityItem("unsure", "unsure", R.drawable.ic_emotion_unsure, "emotion"));
        emotions.add(new ActivityItem("bored", "bored", R.drawable.ic_emotion_bored, "emotion"));
        emotions.add(new ActivityItem("anxious", "anxious", R.drawable.ic_emotion_anxious, "emotion"));
        emotions.add(new ActivityItem("angry", "angry", R.drawable.ic_emotion_angry, "emotion"));
        
        // More negative emotions
        emotions.add(new ActivityItem("stressed", "stressed", R.drawable.ic_emotion_stressed, "emotion"));
        emotions.add(new ActivityItem("sad", "sad", R.drawable.ic_emotion_sad, "emotion"));
        emotions.add(new ActivityItem("desperate", "desperate", R.drawable.ic_emotion_desperate, "emotion"));
        
        return emotions;
    }

    /**
     * Get default list of sleep options (as shown in the screenshots)
     */
    public static List<ActivityItem> getDefaultSleepOptions() {
        List<ActivityItem> sleepOptions = new ArrayList<>();
        
        sleepOptions.add(new ActivityItem("good_sleep", "good sleep", R.drawable.ic_sleep_good, "sleep"));
        sleepOptions.add(new ActivityItem("medium_sleep", "medium sleep", R.drawable.ic_sleep_medium, "sleep"));
        sleepOptions.add(new ActivityItem("bad_sleep", "bad sleep", R.drawable.ic_sleep_bad, "sleep"));
        sleepOptions.add(new ActivityItem("sleep_early", "sleep early", R.drawable.ic_sleep_early, "sleep"));
        
        return sleepOptions;
    }

    /**
     * Get default list of activities
     */
    public static List<ActivityItem> getDefaultActivities() {
        List<ActivityItem> activities = new ArrayList<>();
        
        activities.add(new ActivityItem("exercise", "exercise", R.drawable.ic_activity_exercise, "activity"));
        activities.add(new ActivityItem("reading", "reading", R.drawable.ic_activity_reading, "activity"));
        activities.add(new ActivityItem("music", "music", R.drawable.ic_activity_music, "activity"));
        activities.add(new ActivityItem("friends", "friends", R.drawable.ic_activity_friends, "activity"));
        activities.add(new ActivityItem("work", "work", R.drawable.ic_activity_work, "activity"));
        activities.add(new ActivityItem("shopping", "shopping", R.drawable.ic_activity_shopping, "activity"));
        activities.add(new ActivityItem("cooking", "cooking", R.drawable.ic_activity_cooking, "activity"));
        activities.add(new ActivityItem("gaming", "gaming", R.drawable.ic_activity_gaming, "activity"));
        
        return activities;
    }
}
