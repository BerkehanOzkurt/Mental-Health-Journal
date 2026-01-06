package gui.ceng.mu.edu.mentalhealthjournal.util;

import gui.ceng.mu.edu.mentalhealthjournal.R;

/**
 * Utility class for mood-related operations.
 * Centralizes all mood icon, background, text, and color mappings.
 * 
 * Mood Levels:
 * 5 = Very Good
 * 4 = Good
 * 3 = Normal/Okay
 * 2 = Bad
 * 1 = Very Bad
 */
public final class MoodUtils {

    // Prevent instantiation
    private MoodUtils() {}

    /**
     * Get the drawable resource ID for a mood level's icon.
     * @param moodLevel Mood level (1-5)
     * @return Drawable resource ID
     */
    public static int getIcon(int moodLevel) {
        switch (moodLevel) {
            case 5: return R.drawable.face1;
            case 4: return R.drawable.face2;
            case 3: return R.drawable.face3;
            case 2: return R.drawable.face4;
            case 1:
            default: return R.drawable.face5;
        }
    }

    /**
     * Get the background drawable resource ID for a mood level.
     * @param moodLevel Mood level (1-5)
     * @return Background drawable resource ID
     */
    public static int getBackground(int moodLevel) {
        switch (moodLevel) {
            case 5: return R.drawable.emoji_background_very_good;
            case 4: return R.drawable.emoji_background_good;
            case 3: return R.drawable.emoji_background_normal;
            case 2: return R.drawable.emoji_background_bad;
            case 1:
            default: return R.drawable.emoji_background_very_bad;
        }
    }

    /**
     * Get the display text for a mood level.
     * @param moodLevel Mood level (1-5)
     * @return Human-readable mood text
     */
    public static String getText(int moodLevel) {
        switch (moodLevel) {
            case 5: return "Feeling Great";
            case 4: return "Feeling Good";
            case 3: return "Feeling Okay";
            case 2: return "Feeling Bad";
            case 1: return "Feeling Awful";
            default: return "Unknown";
        }
    }

    /**
     * Get the short label for a mood level.
     * @param moodLevel Mood level (1-5)
     * @return Short mood label
     */
    public static String getLabel(int moodLevel) {
        switch (moodLevel) {
            case 5: return "Very Good";
            case 4: return "Good";
            case 3: return "Okay";
            case 2: return "Bad";
            case 1: return "Very Bad";
            default: return "Unknown";
        }
    }

    /**
     * Get the color resource ID for a mood level.
     * @param moodLevel Mood level (1-5)
     * @return Color resource ID
     */
    public static int getColorRes(int moodLevel) {
        switch (moodLevel) {
            case 5: return R.color.very_good;
            case 4: return R.color.good;
            case 3: return R.color.normal;
            case 2: return R.color.bad;
            case 1:
            default: return R.color.very_bad;
        }
    }

    /**
     * Get the hex color value for a mood level (for charts).
     * @param moodLevel Mood level (1-5)
     * @return Hex color value
     */
    public static int getColorValue(int moodLevel) {
        switch (moodLevel) {
            case 5: return 0xFF4CAF50; // Green
            case 4: return 0xFF8BC34A; // Light Green
            case 3: return 0xFFFFC107; // Amber
            case 2: return 0xFFFF9800; // Orange
            case 1:
            default: return 0xFFF44336; // Red
        }
    }

    /**
     * Validate if a mood level is within valid range.
     * @param moodLevel Mood level to validate
     * @return true if valid (1-5), false otherwise
     */
    public static boolean isValidMoodLevel(int moodLevel) {
        return moodLevel >= 1 && moodLevel <= 5;
    }

    /**
     * Clamp a mood level to valid range.
     * @param moodLevel Mood level to clamp
     * @return Clamped mood level (1-5)
     */
    public static int clamp(int moodLevel) {
        return Math.max(1, Math.min(5, moodLevel));
    }
}
