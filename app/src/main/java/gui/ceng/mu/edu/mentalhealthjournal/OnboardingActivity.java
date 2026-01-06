package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import gui.ceng.mu.edu.mentalhealthjournal.util.OnboardingManager;

/**
 * Full-screen onboarding activity to capture user's name on first launch.
 */
public class OnboardingActivity extends AppCompatActivity {

    public static final String EXTRA_PROCEED_TO_PIN = "proceed_to_pin";

    private OnboardingManager onboardingManager;
    private TextInputEditText nameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        onboardingManager = new OnboardingManager(this);
        initViews();
    }

    private void initViews() {
        nameInput = findViewById(R.id.input_user_name);
        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);
        TextView btnSkip = findViewById(R.id.btn_skip);

        btnGetStarted.setOnClickListener(v -> {
            String name = "";
            if (nameInput.getText() != null) {
                name = nameInput.getText().toString().trim();
            }
            if (name.isEmpty()) {
                name = "Friend";
            }
            completeOnboarding(name, true);
        });

        btnSkip.setOnClickListener(v -> {
            // Skip with default name and go directly to main
            completeOnboarding("Friend", false);
        });
    }

    /**
     * Complete onboarding and proceed to next screen.
     * @param userName The user's name
     * @param proceedToPin Whether to show PIN setup next
     */
    private void completeOnboarding(String userName, boolean proceedToPin) {
        onboardingManager.completeOnboarding(userName);

        if (proceedToPin) {
            // Go to PIN setup
            Intent intent = new Intent(this, PinActivity.class);
            intent.putExtra(PinActivity.EXTRA_FROM_ONBOARDING, true);
            startActivity(intent);
        } else {
            // Skip PIN and go directly to main
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back from onboarding
    }
}
