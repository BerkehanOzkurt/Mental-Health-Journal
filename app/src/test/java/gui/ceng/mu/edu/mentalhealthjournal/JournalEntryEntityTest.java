package gui.ceng.mu.edu.mentalhealthjournal;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;

import static org.junit.Assert.*;

/**
 * Unit tests for JournalEntryEntity class.
 * Tests the model class functionality including getters, setters, and helper methods.
 */
public class JournalEntryEntityTest {

    private JournalEntryEntity entry;

    @Before
    public void setUp() {
        entry = new JournalEntryEntity();
    }

    @Test
    public void testDefaultConstructor() {
        JournalEntryEntity newEntry = new JournalEntryEntity();
        assertNotNull(newEntry);
        assertEquals(0, newEntry.getId());
        assertEquals(0, newEntry.getTimestamp());
        assertEquals(0, newEntry.getMoodLevel());
    }

    @Test
    public void testParameterizedConstructor() {
        long timestamp = System.currentTimeMillis();
        int moodLevel = 4;
        JournalEntryEntity newEntry = new JournalEntryEntity(timestamp, moodLevel);
        
        assertEquals(timestamp, newEntry.getTimestamp());
        assertEquals(moodLevel, newEntry.getMoodLevel());
    }

    @Test
    public void testSetAndGetId() {
        long id = 123L;
        entry.setId(id);
        assertEquals(id, entry.getId());
    }

    @Test
    public void testSetAndGetTimestamp() {
        long timestamp = 1704153600000L; // January 2, 2024
        entry.setTimestamp(timestamp);
        assertEquals(timestamp, entry.getTimestamp());
    }

    @Test
    public void testSetAndGetMoodLevel() {
        entry.setMoodLevel(5);
        assertEquals(5, entry.getMoodLevel());

        entry.setMoodLevel(1);
        assertEquals(1, entry.getMoodLevel());
    }

    @Test
    public void testMoodLevelBoundaries() {
        // Test minimum mood level
        entry.setMoodLevel(1);
        assertEquals(1, entry.getMoodLevel());

        // Test maximum mood level
        entry.setMoodLevel(5);
        assertEquals(5, entry.getMoodLevel());
    }

    @Test
    public void testSetAndGetEmotions() {
        List<String> emotions = Arrays.asList("happy", "excited", "grateful");
        entry.setEmotions(emotions);
        
        List<String> retrieved = entry.getEmotions();
        assertNotNull(retrieved);
        assertEquals(3, retrieved.size());
        assertTrue(retrieved.contains("happy"));
        assertTrue(retrieved.contains("excited"));
        assertTrue(retrieved.contains("grateful"));
    }

    @Test
    public void testSetAndGetEmotionsEmpty() {
        entry.setEmotions(new ArrayList<>());
        assertNotNull(entry.getEmotions());
        assertTrue(entry.getEmotions().isEmpty());
    }

    @Test
    public void testSetAndGetEmotionsNull() {
        entry.setEmotions(null);
        assertNull(entry.getEmotions());
    }

    @Test
    public void testSetAndGetSleepTags() {
        List<String> sleepTags = Arrays.asList("good sleep", "sleep early");
        entry.setSleepTags(sleepTags);
        
        List<String> retrieved = entry.getSleepTags();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertTrue(retrieved.contains("good sleep"));
    }

    @Test
    public void testSetAndGetActivities() {
        List<String> activities = Arrays.asList("exercise", "reading", "music");
        entry.setActivities(activities);
        
        List<String> retrieved = entry.getActivities();
        assertNotNull(retrieved);
        assertEquals(3, retrieved.size());
    }

    @Test
    public void testSetAndGetNote() {
        String note = "Today was a great day! I felt productive and happy.";
        entry.setNote(note);
        assertEquals(note, entry.getNote());
    }

    @Test
    public void testSetAndGetNoteEmpty() {
        entry.setNote("");
        assertEquals("", entry.getNote());
    }

    @Test
    public void testSetAndGetNoteNull() {
        entry.setNote(null);
        assertNull(entry.getNote());
    }

    @Test
    public void testSetAndGetPhotoPath() {
        String photoPath = "/storage/emulated/0/Pictures/journal_photo_123.jpg";
        entry.setPhotoPath(photoPath);
        assertEquals(photoPath, entry.getPhotoPath());
    }

    @Test
    public void testSetAndGetVoiceMemoPath() {
        String voiceMemoPath = "/storage/emulated/0/Recordings/memo_123.m4a";
        entry.setVoiceMemoPath(voiceMemoPath);
        assertEquals(voiceMemoPath, entry.getVoiceMemoPath());
    }

    @Test
    public void testGetMoodIconResource_VeryGood() {
        entry.setMoodLevel(5);
        assertEquals(R.drawable.face1, entry.getMoodIconResource());
    }

    @Test
    public void testGetMoodIconResource_Good() {
        entry.setMoodLevel(4);
        assertEquals(R.drawable.face2, entry.getMoodIconResource());
    }

    @Test
    public void testGetMoodIconResource_Normal() {
        entry.setMoodLevel(3);
        assertEquals(R.drawable.face3, entry.getMoodIconResource());
    }

    @Test
    public void testGetMoodIconResource_Bad() {
        entry.setMoodLevel(2);
        assertEquals(R.drawable.face4, entry.getMoodIconResource());
    }

    @Test
    public void testGetMoodIconResource_VeryBad() {
        entry.setMoodLevel(1);
        assertEquals(R.drawable.face5, entry.getMoodIconResource());
    }

    @Test
    public void testGetMoodIconResource_Default() {
        // For any invalid mood level, should return very bad
        entry.setMoodLevel(0);
        assertEquals(R.drawable.face5, entry.getMoodIconResource());
    }

    @Test
    public void testGetMoodBackgroundResource_VeryGood() {
        entry.setMoodLevel(5);
        assertEquals(R.drawable.emoji_background_very_good, entry.getMoodBackgroundResource());
    }

    @Test
    public void testGetMoodBackgroundResource_Good() {
        entry.setMoodLevel(4);
        assertEquals(R.drawable.emoji_background_good, entry.getMoodBackgroundResource());
    }

    @Test
    public void testGetMoodBackgroundResource_Normal() {
        entry.setMoodLevel(3);
        assertEquals(R.drawable.emoji_background_normal, entry.getMoodBackgroundResource());
    }

    @Test
    public void testGetMoodBackgroundResource_Bad() {
        entry.setMoodLevel(2);
        assertEquals(R.drawable.emoji_background_bad, entry.getMoodBackgroundResource());
    }

    @Test
    public void testGetMoodBackgroundResource_VeryBad() {
        entry.setMoodLevel(1);
        assertEquals(R.drawable.emoji_background_very_bad, entry.getMoodBackgroundResource());
    }

    @Test
    public void testCompleteEntryCreation() {
        long timestamp = System.currentTimeMillis();
        entry.setTimestamp(timestamp);
        entry.setMoodLevel(4);
        entry.setEmotions(Arrays.asList("happy", "grateful"));
        entry.setSleepTags(Arrays.asList("good sleep"));
        entry.setActivities(Arrays.asList("exercise", "reading"));
        entry.setNote("Had a productive day!");
        entry.setPhotoPath("/path/to/photo.jpg");
        entry.setVoiceMemoPath("/path/to/memo.m4a");

        assertEquals(timestamp, entry.getTimestamp());
        assertEquals(4, entry.getMoodLevel());
        assertEquals(2, entry.getEmotions().size());
        assertEquals(1, entry.getSleepTags().size());
        assertEquals(2, entry.getActivities().size());
        assertNotNull(entry.getNote());
        assertNotNull(entry.getPhotoPath());
        assertNotNull(entry.getVoiceMemoPath());
    }
}
