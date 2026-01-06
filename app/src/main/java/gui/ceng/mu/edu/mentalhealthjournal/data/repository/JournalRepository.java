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

    // ========== Statistics Methods ==========

    /**
     * Get photo count in date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @param callback Callback to receive the count
     */
    public void getPhotoCountInRange(long startTime, long endTime, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getPhotoCountInRange(startTime, endTime);
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
     * Get voice memo count in date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @param callback Callback to receive the count
     */
    public void getVoiceMemoCountInRange(long startTime, long endTime, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getVoiceMemoCountInRange(startTime, endTime);
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
     * Get total photo count
     * @param callback Callback to receive the count
     */
    public void getTotalPhotoCount(RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getTotalPhotoCount();
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
     * Get total voice memo count
     * @param callback Callback to receive the count
     */
    public void getTotalVoiceMemoCount(RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getTotalVoiceMemoCount();
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
     * Get entry count in date range
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @param callback Callback to receive the count
     */
    public void getEntryCountInRange(long startTime, long endTime, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getEntryCountInRange(startTime, endTime);
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
     * Get mood count by level in date range
     * @param moodLevel The mood level (1-5)
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @param callback Callback to receive the count
     */
    public void getMoodCountInRange(int moodLevel, long startTime, long endTime, RepositoryCallback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int count = journalEntryDao.getMoodCountInRange(moodLevel, startTime, endTime);
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
     * Get entries in date range synchronously
     * @param startTime Start timestamp
     * @param endTime End timestamp
     * @param callback Callback to receive the entries
     */
    public void getEntriesInRangeSync(long startTime, long endTime, RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getEntriesInRangeSync(startTime, endTime);
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

    // ========== Search and Filter Methods ==========

    /**
     * Search entries by keyword in note, emotions, and activities
     * @param keyword The search keyword
     * @param callback Callback to receive matching entries
     */
    public void searchEntriesByKeyword(String keyword, RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.searchEntriesByKeyword(keyword);
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
     * Get entries with photos
     * @param callback Callback to receive entries with photos
     */
    public void getEntriesWithPhotos(RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getEntriesWithPhotos();
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
     * Get entries with voice memos
     * @param callback Callback to receive entries with voice memos
     */
    public void getEntriesWithVoiceMemos(RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getEntriesWithVoiceMemos();
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
     * Get entries by mood level
     * @param moodLevel The mood level (1-5)
     * @param callback Callback to receive entries
     */
    public void getEntriesByMoodLevel(int moodLevel, RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getEntriesByMoodLevel(moodLevel);
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
     * Get entries by multiple mood levels
     * @param moodLevels List of mood levels to filter by
     * @param callback Callback to receive entries
     */
    public void getEntriesByMoodLevels(List<Integer> moodLevels, RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getEntriesByMoodLevels(moodLevels);
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
     * Get all entries synchronously
     * @param callback Callback to receive all entries
     */
    public void getAllEntriesSync(RepositoryCallback<List<JournalEntryEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<JournalEntryEntity> entries = journalEntryDao.getAllEntriesSync();
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
}
