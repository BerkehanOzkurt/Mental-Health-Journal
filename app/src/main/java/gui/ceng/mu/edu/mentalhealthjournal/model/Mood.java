package gui.ceng.mu.edu.mentalhealthjournal.model;

import androidx.annotation.NonNull;

/**
 * Immutable mood data class.
 * Represents a mood/emotion that users can select when creating journal entries.
 */
public class Mood {
    
    private final String name;
    private final String emoji;
    private final int colorResId;
    private final boolean isDefault;

    public Mood(@NonNull String name, @NonNull String emoji, int colorResId, boolean isDefault) {
        this.name = name;
        this.emoji = emoji;
        this.colorResId = colorResId;
        this.isDefault = isDefault;
    }

    /**
     * Creates a custom (non-default) mood.
     */
    public static Mood createCustom(@NonNull String name, @NonNull String emoji, int colorResId) {
        return new Mood(name, emoji, colorResId, false);
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getEmoji() {
        return emoji;
    }

    public int getColorResId() {
        return colorResId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mood mood = (Mood) o;
        return name.equalsIgnoreCase(mood.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return emoji + " " + name;
    }
}
