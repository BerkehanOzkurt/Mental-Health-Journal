package gui.ceng.mu.edu.mentalhealthjournal.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import gui.ceng.mu.edu.mentalhealthjournal.data.converter.StringListConverter;
import gui.ceng.mu.edu.mentalhealthjournal.data.dao.JournalEntryDao;
import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;

/**
 * Room Database class for the Mental Health Journal app.
 * Singleton pattern to ensure only one instance of the database exists.
 */
@Database(
    entities = {JournalEntryEntity.class},
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter.class)
public abstract class JournalDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "mental_health_journal.db";
    private static volatile JournalDatabase INSTANCE;

    /**
     * Get the JournalEntryDao
     * @return The DAO for journal entries
     */
    public abstract JournalEntryDao journalEntryDao();

    /**
     * Get the singleton instance of the database.
     * Uses double-checked locking for thread safety.
     * 
     * @param context Application context
     * @return The database instance
     */
    public static JournalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (JournalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            JournalDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
