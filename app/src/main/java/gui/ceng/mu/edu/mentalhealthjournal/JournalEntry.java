package gui.ceng.mu.edu.mentalhealthjournal;

public class JournalEntry {
    private long id;
    private String title;
    private String date;
    private int moodIcon;
    private int moodBackground;
    private int moodLevel;

    public JournalEntry(long id, String title, String date, int moodIcon, int moodBackground, int moodLevel) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.moodIcon = moodIcon;
        this.moodBackground = moodBackground;
        this.moodLevel = moodLevel;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getMoodIcon() { return moodIcon; }
    public int getMoodBackground() { return moodBackground; }
    public int getMoodLevel() { return moodLevel; }
}
