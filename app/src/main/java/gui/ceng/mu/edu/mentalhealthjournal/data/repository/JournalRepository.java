package gui.ceng.mu.edu.mentalhealthjournal.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import gui.ceng.mu.edu.mentalhealthjournal.data.dao.JournalEntryDao;
import gui.ceng.mu.edu.mentalhealthjournal.data.database.JournalDatabase;
import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository class that abstracts the data layer.
 * Uses ExecutorService for background thread operations.
 * This satisfies the "background thread implementation" requirement.
 */
public class JournalRepository {

    private final JournalEntryDao journalEntryDao;
    
    // Background thread executor for database operations
    // Using a fixed thread pool with 2 threads for database operations
    private final ExecutorService executorService;

    /**
     * Callback interface for async operations
     */
    public interface RepositoryCallback<T> {
        void onComplete(T result);
        void onError(Exception e);
    }

    public JournalRepository(Context context) {
        JournalDatabase database = JournalDatabase.getInstance(context);
        journalEntryDao = database.journalEntryDao();
        executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * Insert a new journal entry in background thread
     * @param entry The entry to insert
     * @param callback Callback to receive the inserted entry's ID
     */
    public void insert(JournalEntryEntity entry, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = journalEntryDao.insert(entry);
                if (callback != null) {
                    callback.onComplete(id);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Insert a new journal entry in background thread (without callback)
     * @param entry The entry to insert
     */
    public void insert(JournalEntryEntity entry) {
        executorService.execute(() -> journalEntryDao.insert(entry));
    }

    /**
     * Update an existing journal entry in background thread
     * @param entry The entry to update
     */
    public void update(JournalEntryEntity entry) {
        executorService.execute(() -> journalEntryDao.update(entry));
    }

    /**
     * Update an existing journal entry in background thread with callback
     * @param entry The entry to update
     * @param callback Callback when complete
     */
    public void update(JournalEntryEntity entry, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                journalEntryDao.update(entry);
                if (callback != null) {
                    callback.onComplete(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Delete a journal entry in background thread
     * @param entry The entry to delete
     */
    public void delete(JournalEntryEntity entry) {
        executorService.execute(() -> journalEntryDao.delete(entry));
    }

    /**
     * Delete a journal entry by ID in background thread
     * @param id The ID of the entry to delete
     */
    public void deleteById(long id) {
        executorService.execute(() -> journalEntryDao.deleteById(id));
    }

    /**
     * Get all journal entries as LiveData (automatically observed on main thread)
     * @return LiveData list of all entries
     */
    public LiveData<List<JournalEntryEntity>> getAllEntries() {
        return journalEntryDao.getAllEntries();
    }

    /**
     * Get recent entries as LiveData
     * @param limit Maximum number of entries
     * @return LiveData list of recent entries
     */
    public LiveData<List<JournalEntryEntity>> getRecentEntriesLive(int limit) {
        return journalEntryDao.getRecentEntriesLive(limit);
    }

    /**
     * Get a specific journal entry by ID in background thread
     * @param id The ID of the entry
     * @param callback Callback to receive the entry
     */
    public void getEntryById(long id, RepositoryCallback<JournalEntryEntity> callback) {
        executorService.execute(() -> {
            try {
                JournalEntryEntity entry = journalEntryDao.getEntryById(id);
                if (callback != null) {
                    callback.onComplete(entry);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Get entries for a specific date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @return LiveData list of entries
     */
    public LiveData<List<JournalEntryEntity>> getEntriesByDateRange(long startTime, long endTime) {
        return journalEntryDao.getEntriesByDateRange(startTime, endTime);
    }

    /**
     * Get recent entries synchronously in background thread
     * @param limit Maximum number of entries
     * @param callback Callback to receive entries
     */
    public void getRecentEntries(int limit, RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getRecentEntries(limit);
                if (callback != null) {
                    callback.onComplete(entries);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Get entry count in background thread
     * @param callback Callback to receive the count
     */
    public void getEntryCount(RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getEntryCount();
                if (callback != null) {
                    callback.onComplete(count);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Get entry count by mood level in background thread
     * @param moodLevel The mood level (1-5)
     * @param callback Callback to receive the count
     */
    public void getEntryCountByMood(int moodLevel, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getEntryCountByMood(moodLevel);
                if (callback != null) {
                    callback.onComplete(count);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Get average mood for statistics in background thread
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @param callback Callback to receive the average
     */
    public void getAverageMood(long startTime, long endTime, RepositoryCallback<Float> callback) {
        executorService.execute(() -> {
            try {
                float average = journalEntryDao.getAverageMood(startTime, endTime);
                if (callback != null) {
                    callback.onComplete(average);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * Delete all entries in background thread
     */
    public void deleteAllEntries() {
        executorService.execute(() -> journalEntryDao.deleteAllEntries());
    }

    /**
     * Shutdown the executor service when no longer needed
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
