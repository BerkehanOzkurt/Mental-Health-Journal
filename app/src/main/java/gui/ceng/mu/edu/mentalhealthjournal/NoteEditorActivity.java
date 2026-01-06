package gui.ceng.mu.edu.mentalhealthjournal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

/**
 * Full-screen note editor with auto-save capability.
 * Implements clean separation of UI and business logic.
 */
public class NoteEditorActivity extends AppCompatActivity {

    private static final String EXTRA_INITIAL_NOTE = "initial_note";
    private static final String EXTRA_ENTRY_ID = "entry_id";
    public static final String RESULT_NOTE = "result_note";
    
    private static final int MAX_CHARACTERS = 10000;
    private static final long AUTO_SAVE_DELAY_MS = 3000;

    private TextInputEditText noteEditText;
    private TextView characterCountText;
    private TextView autoSaveIndicator;
    private ExtendedFloatingActionButton saveButton;
    
    private Handler autoSaveHandler;
    private Runnable autoSaveRunnable;
    
    private String initialNote = "";
    private boolean hasUnsavedChanges = false;
    private boolean autoSaveEnabled = true;

    /**
     * Creates an intent to launch the NoteEditorActivity.
     * @param context The context
     * @param entryId The journal entry ID (0 for new entries)
     * @param initialNote The initial note content
     * @return Intent configured for NoteEditorActivity
     */
    public static Intent createIntent(Context context, int entryId, String initialNote) {
        Intent intent = new Intent(context, NoteEditorActivity.class);
        intent.putExtra(EXTRA_ENTRY_ID, entryId);
        intent.putExtra(EXTRA_INITIAL_NOTE, initialNote != null ? initialNote : "");
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply stored theme
        SettingsActivity.applyStoredTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        initializeViews();
        setupToolbar();
        setupTextWatcher();
        setupSaveButton();
        loadInitialNote();
        
        autoSaveHandler = new Handler(Looper.getMainLooper());
        autoSaveRunnable = this::performAutoSave;
    }

    private void initializeViews() {
        noteEditText = findViewById(R.id.note_edit_text);
        characterCountText = findViewById(R.id.character_count);
        autoSaveIndicator = findViewById(R.id.auto_save_indicator);
        saveButton = findViewById(R.id.save_button);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> handleBackPress());
    }

    private void setupTextWatcher() {
        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCharacterCount(s.length());
                markAsChanged();
                scheduleAutoSave();
            }
        });
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> saveAndFinish());
    }

    private void loadInitialNote() {
        initialNote = getIntent().getStringExtra(EXTRA_INITIAL_NOTE);
        if (initialNote == null) {
            initialNote = "";
        }
        
        noteEditText.setText(initialNote);
        noteEditText.setSelection(initialNote.length()); // Move cursor to end
        updateCharacterCount(initialNote.length());
        hasUnsavedChanges = false;
    }

    private void updateCharacterCount(int count) {
        characterCountText.setText(String.format(Locale.getDefault(),
                "%d / %d", count, MAX_CHARACTERS));

        // Visual feedback when approaching limit
        if (count > MAX_CHARACTERS * 0.9) {
            characterCountText.setTextColor(getColor(R.color.warning_orange));
        } else if (count > MAX_CHARACTERS * 0.75) {
            characterCountText.setTextColor(getColor(R.color.warning_yellow));
        } else {
            characterCountText.setTextColor(getColor(com.google.android.material.R.color.material_on_surface_emphasis_medium));
        }
    }

    private void markAsChanged() {
        String currentText = getCurrentNote();
        hasUnsavedChanges = !currentText.equals(initialNote);
        
        // Hide auto-save indicator when there are new changes
        autoSaveIndicator.setVisibility(View.GONE);
    }

    private void scheduleAutoSave() {
        if (!autoSaveEnabled) return;
        
        // Cancel any pending auto-save
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
        
        // Schedule new auto-save
        autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY_MS);
    }

    private void performAutoSave() {
        if (hasUnsavedChanges) {
            // Show auto-save indicator
            autoSaveIndicator.setText(R.string.auto_saved);
            autoSaveIndicator.setVisibility(View.VISIBLE);
            
            // Update initial note to current (marks as "saved")
            initialNote = getCurrentNote();
            hasUnsavedChanges = false;
        }
    }

    @NonNull
    private String getCurrentNote() {
        Editable text = noteEditText.getText();
        return text != null ? text.toString() : "";
    }

    private void saveAndFinish() {
        String note = getCurrentNote();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_NOTE, note);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void handleBackPress() {
        String currentNote = getCurrentNote();
        
        // Check if there are actual unsaved changes compared to what we started with
        // or compared to last auto-save
        if (!currentNote.equals(initialNote)) {
            showDiscardDialog();
        } else {
            // Return whatever was the last saved state
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_NOTE, initialNote);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    }

    private void showDiscardDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.discard_changes)
                .setMessage(R.string.discard_message)
                .setPositiveButton(R.string.discard, (dialog, which) -> {
                    // Return original note without changes
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_NOTE, getIntent().getStringExtra(EXTRA_INITIAL_NOTE));
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    finish();
                })
                .setNegativeButton(R.string.keep_editing, null)
                .setNeutralButton(R.string.save, (dialog, which) -> saveAndFinish())
                .show();
    }

    @Override
    public void onBackPressed() {
        handleBackPress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up handler to prevent memory leaks
        if (autoSaveHandler != null) {
            autoSaveHandler.removeCallbacks(autoSaveRunnable);
        }
    }
}
