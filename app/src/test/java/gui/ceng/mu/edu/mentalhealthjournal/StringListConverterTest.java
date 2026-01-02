package gui.ceng.mu.edu.mentalhealthjournal;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gui.ceng.mu.edu.mentalhealthjournal.data.converter.StringListConverter;

import static org.junit.Assert.*;

/**
 * Unit tests for StringListConverter class.
 * Tests the Room TypeConverter that converts List<String> to/from comma-separated String.
 */
public class StringListConverterTest {

    @Test
    public void testFromList_NormalList() {
        List<String> list = Arrays.asList("happy", "excited", "grateful");
        String result = StringListConverter.fromList(list);
        
        assertNotNull(result);
        assertEquals("happy,excited,grateful", result);
    }

    @Test
    public void testFromList_SingleItem() {
        List<String> list = Arrays.asList("happy");
        String result = StringListConverter.fromList(list);
        
        assertNotNull(result);
        assertEquals("happy", result);
    }

    @Test
    public void testFromList_EmptyList() {
        List<String> list = new ArrayList<>();
        String result = StringListConverter.fromList(list);
        
        assertNull(result);
    }

    @Test
    public void testFromList_NullList() {
        String result = StringListConverter.fromList(null);
        assertNull(result);
    }

    @Test
    public void testToList_NormalString() {
        String data = "happy,excited,grateful";
        List<String> result = StringListConverter.toList(data);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("happy", result.get(0));
        assertEquals("excited", result.get(1));
        assertEquals("grateful", result.get(2));
    }

    @Test
    public void testToList_SingleItem() {
        String data = "happy";
        List<String> result = StringListConverter.toList(data);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("happy", result.get(0));
    }

    @Test
    public void testToList_EmptyString() {
        String data = "";
        List<String> result = StringListConverter.toList(data);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToList_NullString() {
        List<String> result = StringListConverter.toList(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testRoundTrip_Conversion() {
        // Test that converting list -> string -> list preserves data
        List<String> original = Arrays.asList("happy", "excited", "grateful", "relaxed");
        
        String stringRepresentation = StringListConverter.fromList(original);
        List<String> reconstructed = StringListConverter.toList(stringRepresentation);
        
        assertNotNull(reconstructed);
        assertEquals(original.size(), reconstructed.size());
        
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), reconstructed.get(i));
        }
    }

    @Test
    public void testFromList_WithSpaces() {
        List<String> list = Arrays.asList("good sleep", "medium sleep", "bad sleep");
        String result = StringListConverter.fromList(list);
        
        assertNotNull(result);
        assertEquals("good sleep,medium sleep,bad sleep", result);
    }

    @Test
    public void testToList_WithSpaces() {
        String data = "good sleep,medium sleep,bad sleep";
        List<String> result = StringListConverter.toList(data);
        
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("good sleep", result.get(0));
        assertEquals("medium sleep", result.get(1));
        assertEquals("bad sleep", result.get(2));
    }
}
