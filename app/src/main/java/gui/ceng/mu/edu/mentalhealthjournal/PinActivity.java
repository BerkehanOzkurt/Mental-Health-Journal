package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import gui.ceng.mu.edu.mentalhealthjournal.util.OnboardingManager;

/**
 * PIN Lock Activity with support for:
 * - PIN verification (normal app launch)
 * - PIN setup (first time enabling)
 * - PIN change (from settings)
 * - First-launch onboarding
 */
public class PinActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_PIN = "user_pin";
    private static final String KEY_PIN_ENABLED = "pin_enabled";
    
    public static final String EXTRA_FROM_ONBOARDING = "from_onboarding";

    private OnboardingManager onboardingManager;

    private enum Mode {
        VERIFY,     // Verify existing PIN
        CREATE,     // Create new PIN
        CONFIRM,    // Confirm new PIN
        CHANGE_VERIFY, // Verify old PIN before change
        CHANGE_NEW  // Enter new PIN for change
    }

    private Mode currentMode = Mode.VERIFY;
    private String enteredPin = "";
    private String newPinToConfirm = "";
    private boolean isFirstLaunchSetup = false;
    
    private SharedPreferences prefs;
    private TextView titleText;
    private TextView subtitleText;
    private ImageView[] pinDots;
    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;
    private ImageButton buttonBackspace;
    private TextView buttonSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        onboardingManager = new OnboardingManager(this);

        // Check for first launch - redirect to OnboardingActivity
        if (onboardingManager.isFirstLaunch()) {
            launchOnboardingActivity();
            return;
        }
        
        // Check if coming from onboarding - show PIN setup with skip option
        boolean fromOnboarding = getIntent().getBooleanExtra(EXTRA_FROM_ONBOARDING, false);
        if (fromOnboarding) {
            initViews();
            currentMode = Mode.CREATE;
            updateUI();
            // Show skip button during first-time PIN creation
            if (buttonSkip != null) {
                buttonSkip.setVisibility(View.VISIBLE);
            }
            return;
        }

        // If PIN is not enabled and not in setup/change mode, skip directly to MainActivity
        boolean pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false);
        boolean setupMode = getIntent().getBooleanExtra("setup_mode", false);
        boolean changePin = getIntent().getBooleanExtra("change_pin", false);
        
        if (!pinEnabled && !setupMode && !changePin) {
            proceedToMain();
            return;
        }
        
        initViews();
        determineMode();
        updateUI();
    }

    /**
     * Launches the OnboardingActivity to capture the user's name.
     */
    private void launchOnboardingActivity() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to MainActivity and finish this activity.
     */
    private void proceedToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        titleText = findViewById(R.id.title_textview);
        subtitleText = findViewById(R.id.subtitle_textview);
        
        pinDots = new ImageView[]{
                findViewById(R.id.pin_dot_1),
                findViewById(R.id.pin_dot_2),
                findViewById(R.id.pin_dot_3),
                findViewById(R.id.pin_dot_4)
        };

        button0 = findViewById(R.id.button0);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);
        button8 = findViewById(R.id.button8);
        button9 = findViewById(R.id.button9);
        buttonBackspace = findViewById(R.id.button_backspace);

        button0.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
        buttonBackspace.setOnClickListener(this);
        
        // Initialize skip button for first-time PIN setup
        buttonSkip = findViewById(R.id.button_skip);
        if (buttonSkip != null) {
            buttonSkip.setOnClickListener(v -> {
                // Skip PIN setup and proceed to main
                proceedToMain();
            });
            // Hide skip button by default (only show during first launch PIN setup)
            buttonSkip.setVisibility(View.GONE);
        }
    }

    private void determineMode() {
        boolean pinEnabled = prefs.getBoolean(KEY_PIN_ENABLED, false);
        String storedPin = prefs.getString(KEY_PIN, null);
        boolean setupMode = getIntent().getBooleanExtra("setup_mode", false);
        boolean changePin = getIntent().getBooleanExtra("change_pin", false);
        
        if (changePin) {
            // Changing PIN - first verify old PIN
            currentMode = Mode.CHANGE_VERIFY;
        } else if (setupMode || !pinEnabled || storedPin == null) {
            // Setting up PIN for first time
            currentMode = Mode.CREATE;
        } else {
            // Normal verification
            currentMode = Mode.VERIFY;
        }
    }

    private void updateUI() {
        switch (currentMode) {
            case VERIFY:
                if (titleText != null) titleText.setText("Enter PIN");
                if (subtitleText != null) subtitleText.setText("Enter your 4-digit PIN to unlock");
                break;
            case CREATE:
                if (titleText != null) titleText.setText("Create PIN");
                if (subtitleText != null) subtitleText.setText("Choose a 4-digit PIN");
                break;
            case CONFIRM:
                if (titleText != null) titleText.setText("Confirm PIN");
                if (subtitleText != null) subtitleText.setText("Re-enter your PIN to confirm");
                break;
            case CHANGE_VERIFY:
                if (titleText != null) titleText.setText("Current PIN");
                if (subtitleText != null) subtitleText.setText("Enter your current PIN");
                break;
            case CHANGE_NEW:
                if (titleText != null) titleText.setText("New PIN");
                if (subtitleText != null) subtitleText.setText("Enter your new PIN");
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_backspace) {
            if (enteredPin.length() > 0) {
                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
            }
        } else {
            if (enteredPin.length() < 4) {
                Button button = (Button) v;
                enteredPin += button.getText().toString();
            }
        }
        updatePinDots();

        if (enteredPin.length() == 4) {
            handlePinEntered();
        }
    }

    private void handlePinEntered() {
        switch (currentMode) {
            case VERIFY:
                verifyPin();
                break;
            case CREATE:
                // Save PIN and ask for confirmation
                newPinToConfirm = enteredPin;
                enteredPin = "";
                currentMode = Mode.CONFIRM;
                updateUI();
                updatePinDots();
                break;
            case CONFIRM:
                confirmNewPin();
                break;
            case CHANGE_VERIFY:
                verifyForChange();
                break;
            case CHANGE_NEW:
                // Save PIN and ask for confirmation
                newPinToConfirm = enteredPin;
                enteredPin = "";
                currentMode = Mode.CONFIRM;
                updateUI();
                updatePinDots();
                break;
        }
    }

    private void verifyPin() {
        String storedPin = prefs.getString(KEY_PIN, null);
        if (enteredPin.equals(storedPin)) {
            // Correct PIN - proceed to main
            proceedToMain();
        } else {
            showError("Incorrect PIN");
        }
    }

    private void confirmNewPin() {
        if (enteredPin.equals(newPinToConfirm)) {
            // PINs match - save
            prefs.edit()
                    .putString(KEY_PIN, enteredPin)
                    .putBoolean(KEY_PIN_ENABLED, true)
                    .apply();
            
            Toast.makeText(this, "PIN created successfully", Toast.LENGTH_SHORT).show();
            
            // If this was a setup or change, go back; otherwise go to main
            if (getIntent().getBooleanExtra("setup_mode", false) || 
                getIntent().getBooleanExtra("change_pin", false)) {
                finish();
            } else {
                proceedToMain();
            }
        } else {
            showError("PINs don't match. Try again.");
            newPinToConfirm = "";
            currentMode = Mode.CREATE;
            updateUI();
        }
    }

    private void verifyForChange() {
        String storedPin = prefs.getString(KEY_PIN, null);
        if (enteredPin.equals(storedPin)) {
            // Correct - proceed to new PIN
            enteredPin = "";
            currentMode = Mode.CHANGE_NEW;
            updateUI();
            updatePinDots();
        } else {
            showError("Incorrect PIN");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        enteredPin = "";
        updatePinDots();
        
        // Shake animation for error feedback
        for (ImageView dot : pinDots) {
            dot.animate()
                    .translationXBy(-10)
                    .setDuration(50)
                    .withEndAction(() -> 
                        dot.animate()
                                .translationXBy(20)
                                .setDuration(50)
                                .withEndAction(() -> 
                                    dot.animate()
                                            .translationXBy(-10)
                                            .setDuration(50)
                                            .start())
                                .start())
                    .start();
        }
    }

    private void updatePinDots() {
        for (int i = 0; i < 4; i++) {
            if (i < enteredPin.length()) {
                pinDots[i].setImageResource(R.drawable.ic_pin_dot_filled);
            } else {
                pinDots[i].setImageResource(R.drawable.ic_pin_dot_empty);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Only allow back press during setup/change modes
        if (currentMode == Mode.CREATE || currentMode == Mode.CONFIRM ||
            currentMode == Mode.CHANGE_VERIFY || currentMode == Mode.CHANGE_NEW) {
            super.onBackPressed();
        }
        // Block back press during normal verification to prevent bypass
    }
}