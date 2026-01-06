package gui.ceng.mu.edu.mentalhealthjournal.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import gui.ceng.mu.edu.mentalhealthjournal.data.converter.StringListConverter;
import gui.ceng.mu.edu.mentalhealthjournal.util.MoodUtils;

import java.util.List;

/**
 * Entity class representing a journal entry in the database.
 * Stores mood, activities, emotions, sleep quality, notes, and media attachments.
 */
@Entity(tableName = "journal_entries")
@TypeConverters(StringListConverter.class)
public class JournalEntryEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    // Timestamp when the entry was created (milliseconds since epoch)
    private long timestamp;

    // Mood level: 1 = Very Bad, 2 = Bad, 3 = Normal, 4 = Good, 5 = Very Good
    private int moodLevel;

    // List of selected emotion names (e.g., "happy", "excited", "anxious")
    private List<String> emotions;

    // List of selected sleep tags (e.g., "good sleep", "bad sleep")
    private List<String> sleepTags;

    // List of selected activity names (e.g., "exercise", "reading")
    private List<String> activities;

    // Quick note text
    private String note;

    // Photo file path (if any)
    private String photoPath;

    // Voice memo file path (if any)
    private String voiceMemoPath;

    // Constructors
    public JournalEntryEntity() {
    }

    public JournalEntryEntity(long timestamp, int moodLevel) {
        this.timestamp = timestamp;
        this.moodLevel = moodLevel;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getMoodLevel() {
        return moodLevel;
    }

    public void setMoodLevel(int moodLevel) {
        this.moodLevel = moodLevel;
    }

    public List<String> getEmotions() {
        return emotions;
    }

    public void setEmotions(List<String> emotions) {
        this.emotions = emotions;
    }

    public List<String> getSleepTags() {
        return sleepTags;
    }

    public void setSleepTags(List<String> sleepTags) {
        this.sleepTags = sleepTags;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getVoiceMemoPath() {
        return voiceMemoPath;
    }

    public void setVoiceMemoPath(String voiceMemoPath) {
        this.voiceMemoPath = voiceMemoPath;
    }

    /**
     * Helper method to get mood icon resource based on mood level
     */
    public int getMoodIconResource() {
        return MoodUtils.getIcon(moodLevel);
    }

    /**
     * Helper method to get mood background resource based on mood level
     */
    public int getMoodBackgroundResource() {
        return MoodUtils.getBackground(moodLevel);
    }
}
