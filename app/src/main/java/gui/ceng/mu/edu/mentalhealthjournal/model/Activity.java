package gui.ceng.mu.edu.mentalhealthjournal.model;

import androidx.annotation.NonNull;

/**
 * Immutable activity data class.
 * Represents an activity that users can select when creating journal entries.
 */
public class Activity {

    private final String name;
    private final String icon;  // Can be emoji or drawable resource name
    private final int colorResId;
    private final boolean isDefault;

    public Activity(@NonNull String name, @NonNull String icon, int colorResId, boolean isDefault) {
        this.name = name;
        this.icon = icon;
        this.colorResId = colorResId;
        this.isDefault = isDefault;
    }

    /**
     * Creates a custom (non-default) activity.
     */
    public static Activity createCustom(@NonNull String name, @NonNull String icon, int colorResId) {
        return new Activity(name, icon, colorResId, false);
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getIcon() {
        return icon;
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
        Activity activity = (Activity) o;
        return name.equalsIgnoreCase(activity.name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return icon + " " + name;
    }
}
