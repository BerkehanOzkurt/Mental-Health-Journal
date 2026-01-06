package gui.ceng.mu.edu.mentalhealthjournal.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.ceng.mu.edu.mentalhealthjournal.data.database.JournalDatabase;
import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;

/**
 * Manages backup and restore operations for journal data.
 * Handles JSON export/import of journal entries.
 */
public class BackupManager {

    private static final String TAG = "BackupManager";
    private static final String BACKUP_FOLDER = "MentalHealthJournal";
    private static final String BACKUP_FILE_PREFIX = "journal_backup_";
    private static final String BACKUP_FILE_EXTENSION = ".json";

    private final Context context;
    private final Gson gson;
    private final ExecutorService executor;

    public interface BackupCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public BackupManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Creates a backup of all journal entries to internal storage.
     * Returns the backup file path on success.
     */
    public void createBackup(@NonNull BackupCallback callback) {
        executor.execute(() -> {
            try {
                // Get all entries from database
                JournalDatabase db = JournalDatabase.getInstance(context);
                List<JournalEntryEntity> entries = db.journalEntryDao().getAllEntriesSync();

                // Create backup data
                BackupData backupData = new BackupData();
                backupData.version = 1;
                backupData.createdAt = System.currentTimeMillis();
                backupData.entries = entries;

                // Convert to JSON
                String json = gson.toJson(backupData);

                // Get backup file
                File backupFile = getBackupFile();
                
                // Write to file
                try (FileOutputStream fos = new FileOutputStream(backupFile);
                     OutputStreamWriter writer = new OutputStreamWriter(fos)) {
                    writer.write(json);
                }

                callback.onSuccess("Backup saved to: " + backupFile.getName());

            } catch (Exception e) {
                callback.onError("Backup failed: " + e.getMessage());
            }
        });
    }

    /**
     * Restores journal entries from the default backup file in internal storage.
     */
    public void restoreFromBackup(@NonNull BackupCallback callback) {
        executor.execute(() -> {
            try {
                File backupFile = getBackupFile();
                
                if (!backupFile.exists()) {
                    callback.onError("No backup file found");
                    return;
                }

                // Read JSON from file
                StringBuilder json = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new java.io.FileInputStream(backupFile)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                }

                // Parse backup data
                BackupData backupData = gson.fromJson(json.toString(), BackupData.class);

                if (backupData == null || backupData.entries == null) {
                    callback.onError("Invalid backup file");
                    return;
                }

                // Clear existing entries and insert restored ones
                JournalDatabase db = JournalDatabase.getInstance(context);
                db.journalEntryDao().deleteAllEntries();
                
                for (JournalEntryEntity entry : backupData.entries) {
                    // Reset ID to let Room auto-generate
                    entry.setId(0);
                    db.journalEntryDao().insert(entry);
                }

                callback.onSuccess("Restored " + backupData.entries.size() + " entries");

            } catch (Exception e) {
                callback.onError("Restore failed: " + e.getMessage());
            }
        });
    }

    /**
     * Exports journal data to a URI (for sharing via system file picker).
     */
    public void exportToUri(@NonNull Uri uri, @NonNull BackupCallback callback) {
        executor.execute(() -> {
            try {
                // Get all entries from database
                JournalDatabase db = JournalDatabase.getInstance(context);
                List<JournalEntryEntity> entries = db.journalEntryDao().getAllEntriesSync();

                // Create export data
                BackupData exportData = new BackupData();
                exportData.version = 1;
                exportData.createdAt = System.currentTimeMillis();
                exportData.entries = entries;

                // Convert to JSON
                String json = gson.toJson(exportData);

                // Write to URI
                try (java.io.OutputStream os = context.getContentResolver().openOutputStream(uri);
                     OutputStreamWriter writer = new OutputStreamWriter(os)) {
                    writer.write(json);
                }

                callback.onSuccess("Exported " + entries.size() + " entries");

            } catch (Exception e) {
                callback.onError("Export failed: " + e.getMessage());
            }
        });
    }

    /**
     * Imports journal data from a URI (from system file picker).
     */
    public void importFromUri(@NonNull Uri uri, @NonNull BackupCallback callback) {
        executor.execute(() -> {
            try {
                // Read JSON from URI
                StringBuilder json = new StringBuilder();
                try (InputStream is = context.getContentResolver().openInputStream(uri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }
                }

                // Parse backup data
                BackupData importData = gson.fromJson(json.toString(), BackupData.class);

                if (importData == null || importData.entries == null) {
                    callback.onError("Invalid backup file format");
                    return;
                }

                // Insert imported entries (merge with existing)
                JournalDatabase db = JournalDatabase.getInstance(context);
                int imported = 0;
                
                for (JournalEntryEntity entry : importData.entries) {
                    // Reset ID to let Room auto-generate (creates new entries)
                    entry.setId(0);
                    db.journalEntryDao().insert(entry);
                    imported++;
                }

                callback.onSuccess("Imported " + imported + " entries");

            } catch (Exception e) {
                callback.onError("Import failed: " + e.getMessage());
            }
        });
    }

    /**
     * Checks if a backup file exists.
     */
    public boolean hasBackup() {
        return getBackupFile().exists();
    }

    /**
     * Gets the backup file path.
     */
    public File getBackupFile() {
        File backupDir = new File(context.getFilesDir(), BACKUP_FOLDER);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return new File(backupDir, "journal_backup.json");
    }

    /**
     * Generates a filename for export.
     */
    public String generateExportFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return BACKUP_FILE_PREFIX + sdf.format(new Date()) + BACKUP_FILE_EXTENSION;
    }

    /**
     * Internal data class for backup structure.
     */
    private static class BackupData {
        int version;
        long createdAt;
        List<JournalEntryEntity> entries;
    }
}
