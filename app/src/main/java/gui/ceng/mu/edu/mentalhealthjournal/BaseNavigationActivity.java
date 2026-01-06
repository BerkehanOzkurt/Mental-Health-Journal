package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base activity that provides common bottom navigation functionality.
 * Extend this class to avoid duplicating navigation setup code.
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup bottom navigation. Call this in onCreate after setContentView.
     * @param selectedItemId The navigation item ID that should be selected
     */
    protected void setupBottomNavigation(int selectedItemId) {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) return;

        bottomNavigationView.setSelectedItemId(selectedItemId);
        bottomNavigationView.setOnItemSelectedListener(item -> handleNavigationItemSelected(item.getItemId(), selectedItemId));
    }

    /**
     * Handle navigation item selection.
     * @param itemId The selected item ID
     * @param currentItemId The current activity's item ID
     * @return true if the event was handled
     */
    private boolean handleNavigationItemSelected(int itemId, int currentItemId) {
        // Don't navigate if already on current screen
        if (itemId == currentItemId) {
            return true;
        }

        Class<?> targetActivity = getActivityForNavItem(itemId);
        if (targetActivity != null) {
            navigateTo(targetActivity);
            return true;
        }
        return false;
    }

    /**
     * Get the activity class for a navigation item ID.
     * @param itemId Navigation item ID
     * @return Activity class or null
     */
    private Class<?> getActivityForNavItem(int itemId) {
        if (itemId == R.id.navigation_home) {
            return MainActivity.class;
        } else if (itemId == R.id.navigation_calendar) {
            return CalendarActivity.class;
        } else if (itemId == R.id.navigation_stats) {
            return StatsActivity.class;
        } else if (itemId == R.id.navigation_more) {
            return MoreActivity.class;
        }
        return null;
    }

    /**
     * Navigate to another activity with proper flags and transitions.
     * @param activityClass Target activity class
     */
    protected void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Navigate to an activity with extras.
     * @param activityClass Target activity class
     * @param extras Bundle with extras to pass
     */
    protected void navigateToWithExtras(Class<?> activityClass, Bundle extras) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure correct navigation item is selected when returning to activity
        if (bottomNavigationView != null) {
            int currentNavItem = getCurrentNavigationItem();
            if (currentNavItem != 0) {
                bottomNavigationView.setSelectedItemId(currentNavItem);
            }
        }
    }

    /**
     * Override this to return the navigation item ID for this activity.
     * @return Navigation item resource ID
     */
    protected abstract int getCurrentNavigationItem();
}
