package gui.ceng.mu.edu.mentalhealthjournal.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;

import java.util.List;

/**
 * Data Access Object for JournalEntryEntity.
 * Provides methods to interact with the journal_entries table.
 */
@Dao
public interface JournalEntryDao {

    /**
     * Insert a new journal entry
     * @param entry The entry to insert
     * @return The row ID of the newly inserted entry
     */
    @Insert
    long insert(JournalEntryEntity entry);

    /**
     * Update an existing journal entry
     * @param entry The entry to update
     */
    @Update
    void update(JournalEntryEntity entry);

    /**
     * Delete a journal entry
     * @param entry The entry to delete
     */
    @Delete
    void delete(JournalEntryEntity entry);

    /**
     * Delete a journal entry by ID
     * @param id The ID of the entry to delete
     */
    @Query("DELETE FROM journal_entries WHERE id = :id")
    void deleteById(long id);

    /**
     * Get all journal entries ordered by timestamp (newest first)
     * @return LiveData list of all entries
     */
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    LiveData<List<JournalEntryEntity>> getAllEntries();

    /**
     * Get all journal entries synchronously (for background operations)
     * @return List of all entries
     */
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    List<JournalEntryEntity> getAllEntriesSync();

    /**
     * Get a specific journal entry by ID
     * @param id The ID of the entry
     * @return The journal entry
     */
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    JournalEntryEntity getEntryById(long id);

    /**
     * Get entries for a specific date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return LiveData list of entries in the date range
     */
    @Query("SELECT * FROM journal_entries WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    LiveData<List<JournalEntryEntity>> getEntriesByDateRange(long startTime, long endTime);

    /**
     * Get entries for a specific date range synchronously
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of entries in the date range
     */
    @Query("SELECT * FROM journal_entries WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    List<JournalEntryEntity> getEntriesByDateRangeSync(long startTime, long endTime);

    /**
     * Get the count of all entries
     * @return Total number of entries
     */
    @Query("SELECT COUNT(*) FROM journal_entries")
    int getEntryCount();

    /**
     * Get the count of entries with a specific mood level
     * @param moodLevel The mood level (1-5)
     * @return Count of entries with that mood level
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE moodLevel = :moodLevel")
    int getEntryCountByMood(int moodLevel);

    /**
     * Get the most recent entries (limited)
     * @param limit Maximum number of entries to return
     * @return List of recent entries
     */
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC LIMIT :limit")
    List<JournalEntryEntity> getRecentEntries(int limit);

    /**
     * Get all entries ordered by timestamp (newest first) - LiveData version for recent entries
     * @param limit Maximum number of entries
     * @return LiveData list of recent entries
     */
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<JournalEntryEntity>> getRecentEntriesLive(int limit);

    /**
     * Delete all entries
     */
    @Query("DELETE FROM journal_entries")
    void deleteAllEntries();

    /**
     * Get average mood for a date range (for statistics)
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Average mood level
     */
    @Query("SELECT AVG(moodLevel) FROM journal_entries WHERE timestamp >= :startTime AND timestamp <= :endTime")
    float getAverageMood(long startTime, long endTime);
}
