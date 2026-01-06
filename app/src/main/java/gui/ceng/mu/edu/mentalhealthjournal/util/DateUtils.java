package gui.ceng.mu.edu.mentalhealthjournal.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date and time operations.
 * Centralizes all date manipulation, formatting, and comparison logic.
 */
public final class DateUtils {

    // Common date format patterns
    public static final String PATTERN_DATE_KEY = "yyyy-MM-dd";
    public static final String PATTERN_FULL_DATE = "MMMM d, yyyy";
    public static final String PATTERN_MONTH_YEAR = "MMMM yyyy";
    public static final String PATTERN_DAY_MONTH = "d MMMM";
    public static final String PATTERN_TIME_24H = "HH:mm";
    public static final String PATTERN_DATETIME = "MMM d, yyyy • HH:mm";
    public static final String PATTERN_WEEKDAY_DATE = "EEEE, d MMMM";

    // Prevent instantiation
    private DateUtils() {}

    /**
     * Get the start of day (00:00:00.000) for a given calendar.
     * @param cal Calendar instance
     * @return Timestamp in milliseconds
     */
    public static long getStartOfDay(Calendar cal) {
        Calendar start = (Calendar) cal.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTimeInMillis();
    }

    /**
     * Get the start of day for a timestamp.
     * @param timestamp Timestamp in milliseconds
     * @return Start of day timestamp
     */
    public static long getStartOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return getStartOfDay(cal);
    }

    /**
     * Get the end of day (23:59:59.999) for a given calendar.
     * @param cal Calendar instance
     * @return Timestamp in milliseconds
     */
    public static long getEndOfDay(Calendar cal) {
        Calendar end = (Calendar) cal.clone();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTimeInMillis();
    }

    /**
     * Get the end of day for a timestamp.
     * @param timestamp Timestamp in milliseconds
     * @return End of day timestamp
     */
    public static long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return getEndOfDay(cal);
    }

    /**
     * Check if a calendar date is today.
     * @param cal Calendar to check
     * @return true if the date is today
     */
    public static boolean isToday(Calendar cal) {
        Calendar today = Calendar.getInstance();
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
               cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Check if a timestamp is from today.
     * @param timestamp Timestamp to check
     * @return true if the timestamp is from today
     */
    public static boolean isToday(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return isToday(cal);
    }

    /**
     * Check if a calendar date is in the future (after today).
     * @param cal Calendar to check
     * @return true if the date is in the future
     */
    public static boolean isFuture(Calendar cal) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        today.set(Calendar.MILLISECOND, 999);
        
        Calendar checkDate = (Calendar) cal.clone();
        checkDate.set(Calendar.HOUR_OF_DAY, 0);
        checkDate.set(Calendar.MINUTE, 0);
        checkDate.set(Calendar.SECOND, 0);
        checkDate.set(Calendar.MILLISECOND, 0);
        
        return checkDate.after(today);
    }

    /**
     * Check if a month/year is in the future.
     * @param year Year to check
     * @param month Month to check (0-based)
     * @return true if the month is in the future
     */
    public static boolean isFutureMonth(int year, int month) {
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int currentMonth = today.get(Calendar.MONTH);
        
        return year > currentYear || (year == currentYear && month > currentMonth);
    }

    /**
     * Format a timestamp using the specified pattern.
     * @param timestamp Timestamp in milliseconds
     * @param pattern Date format pattern
     * @return Formatted date string
     */
    public static String format(long timestamp, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format a calendar using the specified pattern.
     * @param cal Calendar instance
     * @param pattern Date format pattern
     * @return Formatted date string
     */
    public static String format(Calendar cal, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    /**
     * Get a formatted date key (yyyy-MM-dd) for a calendar.
     * @param cal Calendar instance
     * @return Date key string
     */
    public static String getDateKey(Calendar cal) {
        return format(cal, PATTERN_DATE_KEY);
    }

    /**
     * Get a formatted date key (yyyy-MM-dd) for a timestamp.
     * @param timestamp Timestamp in milliseconds
     * @return Date key string
     */
    public static String getDateKey(long timestamp) {
        return format(timestamp, PATTERN_DATE_KEY);
    }

    /**
     * Get the start of current week (Monday).
     * @return Calendar set to start of week
     */
    public static Calendar getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Get the start of current month.
     * @return Calendar set to start of month
     */
    public static Calendar getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Get a calendar for a specific date.
     * @param year Year
     * @param month Month (0-based)
     * @param day Day of month
     * @return Calendar instance
     */
    public static Calendar getCalendar(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal;
    }

    /**
     * Get a calendar for a specific date and time.
     * @param year Year
     * @param month Month (0-based)
     * @param day Day of month
     * @param hour Hour (0-23)
     * @param minute Minute
     * @return Calendar instance
     */
    public static Calendar getCalendar(int year, int month, int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    /**
     * Calculate the number of days between two dates.
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Number of days
     */
    public static int daysBetween(long startTime, long endTime) {
        long diff = endTime - startTime;
        return (int) (diff / (24 * 60 * 60 * 1000));
    }

    /**
     * Get date range for statistics periods.
     * @param periodType 0 = weekly, 1 = monthly, 2 = yearly
     * @return Array with [startTime, endTime]
     */
    public static long[] getDateRangeForPeriod(int periodType) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTime = calendar.getTimeInMillis();

        switch (periodType) {
            case 1: // Monthly
                calendar.add(Calendar.MONTH, -1);
                break;
            case 2: // Yearly
                calendar.add(Calendar.YEAR, -1);
                break;
            default: // Weekly
                calendar.add(Calendar.DAY_OF_YEAR, -6);
                break;
        }
        
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        return new long[]{startTime, endTime};
    }

    /**
     * Format time in 24-hour format.
     */
    public static String formatTime(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    /**
     * Get relative time string (e.g., "2 hours ago", "Yesterday").
     */
    public static String getTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000, hours = minutes / 60, days = hours / 24;
        
        if (days > 7) return format(timestamp, "MMM d");
        if (days > 1) return days + " days ago";
        if (days == 1) return "Yesterday";
        if (hours > 1) return hours + " hours ago";
        if (hours == 1) return "1 hour ago";
        if (minutes > 1) return minutes + " minutes ago";
        return "Just now";
    }
}
