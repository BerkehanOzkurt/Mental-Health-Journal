package gui.ceng.mu.edu.mentalhealthjournal.data.converter;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type converter for Room database to convert List<String> to/from a comma-separated String.
 */
public class StringListConverter {

    private static final String SEPARATOR = ",";

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    @TypeConverter
    public static List<String> toList(String data) {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(data.split(SEPARATOR)));
    }
}
