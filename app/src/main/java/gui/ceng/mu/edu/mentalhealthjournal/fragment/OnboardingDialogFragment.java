package gui.ceng.mu.edu.mentalhealthjournal.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import gui.ceng.mu.edu.mentalhealthjournal.R;

/**
 * Welcome dialog shown on first launch to capture user's name.
 * Uses DialogFragment for proper lifecycle management.
 */
public class OnboardingDialogFragment extends DialogFragment {

    private static final String TAG = "OnboardingDialog";

    /**
     * Callback interface for onboarding completion.
     */
    public interface OnboardingCallback {
        void onOnboardingComplete(String userName);
    }

    private OnboardingCallback callback;

    /**
     * Creates a new instance of the onboarding dialog.
     * @return A new OnboardingDialogFragment instance
     */
    public static OnboardingDialogFragment newInstance() {
        return new OnboardingDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnboardingCallback) {
            callback = (OnboardingCallback) context;
        } else {
            throw new RuntimeException(context + " must implement OnboardingCallback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Prevent dismissal by back button or outside touch
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_onboarding, null);

        TextInputEditText nameInput = view.findViewById(R.id.input_user_name);

        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.get_started, (dialogInterface, which) -> {
                    String name = "";
                    if (nameInput.getText() != null) {
                        name = nameInput.getText().toString().trim();
                    }
                    if (name.isEmpty()) {
                        name = "Friend";
                    }
                    if (callback != null) {
                        callback.onOnboardingComplete(name);
                    }
                })
                .create();

        // Show keyboard automatically when dialog appears
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
