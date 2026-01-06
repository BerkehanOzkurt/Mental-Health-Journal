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

    // ========== Statistics Queries ==========

    /**
     * Count entries with photos in a date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Count of entries with photos
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE photoPath IS NOT NULL AND photoPath != '' AND timestamp >= :startTime AND timestamp <= :endTime")
    int getPhotoCountInRange(long startTime, long endTime);

    /**
     * Count entries with voice memos in a date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Count of entries with voice memos
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE voiceMemoPath IS NOT NULL AND voiceMemoPath != '' AND timestamp >= :startTime AND timestamp <= :endTime")
    int getVoiceMemoCountInRange(long startTime, long endTime);

    /**
     * Count all entries with photos
     * @return Total count of entries with photos
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE photoPath IS NOT NULL AND photoPath != ''")
    int getTotalPhotoCount();

    /**
     * Count all entries with voice memos
     * @return Total count of entries with voice memos
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE voiceMemoPath IS NOT NULL AND voiceMemoPath != ''")
    int getTotalVoiceMemoCount();

    /**
     * Get entries count in a date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Count of entries
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE timestamp >= :startTime AND timestamp <= :endTime")
    int getEntryCountInRange(long startTime, long endTime);

    /**
     * Get mood count by level in a date range
     * @param moodLevel The mood level (1-5)
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return Count of entries with that mood level in range
     */
    @Query("SELECT COUNT(*) FROM journal_entries WHERE moodLevel = :moodLevel AND timestamp >= :startTime AND timestamp <= :endTime")
    int getMoodCountInRange(int moodLevel, long startTime, long endTime);

    /**
     * Get entries in date range synchronously for statistics
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return List of entries
     */
    @Query("SELECT * FROM journal_entries WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    List<JournalEntryEntity> getEntriesInRangeSync(long startTime, long endTime);

    // ========== Search and Filter Queries ==========

    /**
     * Search entries by keyword in note, emotions, and activities
     * @param keyword The search keyword (will be wrapped with % for LIKE query)
     * @return List of matching entries
     */
    @Query("SELECT * FROM journal_entries WHERE " +
           "note LIKE '%' || :keyword || '%' OR " +
           "emotions LIKE '%' || :keyword || '%' OR " +
           "activities LIKE '%' || :keyword || '%' " +
           "ORDER BY timestamp DESC")
    List<JournalEntryEntity> searchEntriesByKeyword(String keyword);

    /**
     * Get entries with photos
     * @return List of entries that have photos attached
     */
    @Query("SELECT * FROM journal_entries WHERE photoPath IS NOT NULL AND photoPath != '' ORDER BY timestamp DESC")
    List<JournalEntryEntity> getEntriesWithPhotos();

    /**
     * Get entries with voice memos
     * @return List of entries that have voice memos attached
     */
    @Query("SELECT * FROM journal_entries WHERE voiceMemoPath IS NOT NULL AND voiceMemoPath != '' ORDER BY timestamp DESC")
    List<JournalEntryEntity> getEntriesWithVoiceMemos();

    /**
     * Get entries by mood level
     * @param moodLevel The mood level (1-5)
     * @return List of entries with that mood level
     */
    @Query("SELECT * FROM journal_entries WHERE moodLevel = :moodLevel ORDER BY timestamp DESC")
    List<JournalEntryEntity> getEntriesByMoodLevel(int moodLevel);

    /**
     * Get entries by multiple mood levels
     * @param moodLevels List of mood levels to filter by
     * @return List of entries with any of the specified mood levels
     */
    @Query("SELECT * FROM journal_entries WHERE moodLevel IN (:moodLevels) ORDER BY timestamp DESC")
    List<JournalEntryEntity> getEntriesByMoodLevels(List<Integer> moodLevels);
}
