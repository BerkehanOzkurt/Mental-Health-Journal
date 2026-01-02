package gui.ceng.mu.edu.mentalhealthjournal;

import org.junit.Test;

import java.util.List;

import gui.ceng.mu.edu.mentalhealthjournal.model.ActivityItem;

import static org.junit.Assert.*;

/**
 * Unit tests for ActivityItem class.
 * Tests the model class and its static factory methods.
 */
public class ActivityItemTest {

    @Test
    public void testConstructor() {
        ActivityItem item = new ActivityItem("happy", "happy", R.drawable.ic_emotion_happy, "emotion");
        
        assertEquals("happy", item.getId());
        assertEquals("happy", item.getName());
        assertEquals(R.drawable.ic_emotion_happy, item.getIconResourceId());
        assertEquals("emotion", item.getCategory());
        assertFalse(item.isSelected());
    }

    @Test
    public void testSetAndGetId() {
        ActivityItem item = new ActivityItem("test", "Test", R.drawable.ic_emotion_happy, "emotion");
        item.setId("newId");
        assertEquals("newId", item.getId());
    }

    @Test
    public void testSetAndGetName() {
        ActivityItem item = new ActivityItem("test", "Test", R.drawable.ic_emotion_happy, "emotion");
        item.setName("New Name");
        assertEquals("New Name", item.getName());
    }

    @Test
    public void testSetAndGetIconResourceId() {
        ActivityItem item = new ActivityItem("test", "Test", R.drawable.ic_emotion_happy, "emotion");
        item.setIconResourceId(R.drawable.ic_emotion_sad);
        assertEquals(R.drawable.ic_emotion_sad, item.getIconResourceId());
    }

    @Test
    public void testSetAndGetCategory() {
        ActivityItem item = new ActivityItem("test", "Test", R.drawable.ic_emotion_happy, "emotion");
        item.setCategory("sleep");
        assertEquals("sleep", item.getCategory());
    }

    @Test
    public void testSetAndGetSelected() {
        ActivityItem item = new ActivityItem("test", "Test", R.drawable.ic_emotion_happy, "emotion");
        
        assertFalse(item.isSelected());
        
        item.setSelected(true);
        assertTrue(item.isSelected());
        
        item.setSelected(false);
        assertFalse(item.isSelected());
    }

    @Test
    public void testToggleSelection() {
        ActivityItem item = new ActivityItem("test", "Test", R.drawable.ic_emotion_happy, "emotion");
        
        assertFalse(item.isSelected());
        
        item.toggleSelection();
        assertTrue(item.isSelected());
        
        item.toggleSelection();
        assertFalse(item.isSelected());
    }

    @Test
    public void testGetDefaultEmotions() {
        List<ActivityItem> emotions = ActivityItem.getDefaultEmotions();
        
        assertNotNull(emotions);
        assertEquals(13, emotions.size());
        
        // Check that all items have "emotion" category
        for (ActivityItem item : emotions) {
            assertEquals("emotion", item.getCategory());
            assertNotNull(item.getId());
            assertNotNull(item.getName());
            assertFalse(item.isSelected());
        }
        
        // Check some specific emotions exist
        assertTrue(emotions.stream().anyMatch(e -> e.getId().equals("happy")));
        assertTrue(emotions.stream().anyMatch(e -> e.getId().equals("excited")));
        assertTrue(emotions.stream().anyMatch(e -> e.getId().equals("anxious")));
        assertTrue(emotions.stream().anyMatch(e -> e.getId().equals("sad")));
    }

    @Test
    public void testGetDefaultSleepOptions() {
        List<ActivityItem> sleepOptions = ActivityItem.getDefaultSleepOptions();
        
        assertNotNull(sleepOptions);
        assertEquals(4, sleepOptions.size());
        
        // Check that all items have "sleep" category
        for (ActivityItem item : sleepOptions) {
            assertEquals("sleep", item.getCategory());
            assertNotNull(item.getId());
            assertNotNull(item.getName());
            assertFalse(item.isSelected());
        }
        
        // Check specific sleep options exist
        assertTrue(sleepOptions.stream().anyMatch(s -> s.getId().equals("good_sleep")));
        assertTrue(sleepOptions.stream().anyMatch(s -> s.getId().equals("bad_sleep")));
    }

    @Test
    public void testGetDefaultActivities() {
        List<ActivityItem> activities = ActivityItem.getDefaultActivities();
        
        assertNotNull(activities);
        assertEquals(8, activities.size());
        
        // Check that all items have "activity" category
        for (ActivityItem item : activities) {
            assertEquals("activity", item.getCategory());
            assertNotNull(item.getId());
            assertNotNull(item.getName());
            assertFalse(item.isSelected());
        }
        
        // Check specific activities exist
        assertTrue(activities.stream().anyMatch(a -> a.getId().equals("exercise")));
        assertTrue(activities.stream().anyMatch(a -> a.getId().equals("reading")));
        assertTrue(activities.stream().anyMatch(a -> a.getId().equals("gaming")));
    }

    @Test
    public void testEmotionItemsNotInitiallySelected() {
        List<ActivityItem> emotions = ActivityItem.getDefaultEmotions();
        
        for (ActivityItem emotion : emotions) {
            assertFalse("Emotion " + emotion.getName() + " should not be selected by default", 
                        emotion.isSelected());
        }
    }

    @Test
    public void testSelectMultipleEmotions() {
        List<ActivityItem> emotions = ActivityItem.getDefaultEmotions();
        
        // Select first 3 emotions
        emotions.get(0).setSelected(true);
        emotions.get(1).setSelected(true);
        emotions.get(2).setSelected(true);
        
        assertTrue(emotions.get(0).isSelected());
        assertTrue(emotions.get(1).isSelected());
        assertTrue(emotions.get(2).isSelected());
        assertFalse(emotions.get(3).isSelected());
    }
}
