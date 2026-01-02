package gui.ceng.mu.edu.mentalhealthjournal;

public class JournalEntry {
    private String title;
    private String date;
    private int moodIcon;
    private int moodBackground;

    public JournalEntry(String title, String date, int moodIcon, int moodBackground) {
        this.title = title;
        this.date = date;
        this.moodIcon = moodIcon;
        this.moodBackground = moodBackground;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getMoodIcon() { return moodIcon; }
    public int getMoodBackground() { return moodBackground; }
}
