package gui.ceng.mu.edu.mentalhealthjournal;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity for selecting mood before adding a journal entry.
 * This is the first step in the entry creation flow.
 * Shows 5 mood options with animations when selected.
 */
public class MoodSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_YEAR = "year";
    
    private ImageView btnVeryGood, btnGood, btnNormal, btnBad, btnVeryBad;
    private MaterialButton btnContinue;
    private TextView tvDate, tvTime;
    
    private int selectedMood = -1; // -1 means no mood selected
    private ImageView currentlySelected = null;
    
    private int day, month, year;
    private long entryTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);
        
        // Get date from intent or use current date
        Calendar calendar = Calendar.getInstance();
        day = getIntent().getIntExtra(EXTRA_DAY, calendar.get(Calendar.DAY_OF_MONTH));
        month = getIntent().getIntExtra(EXTRA_MONTH, calendar.get(Calendar.MONTH));
        year = getIntent().getIntExtra(EXTRA_YEAR, calendar.get(Calendar.YEAR));
        
        // Set the calendar to the selected date
        calendar.set(year, month, day);
        entryTimestamp = calendar.getTimeInMillis();
        
        initViews();
        setupDateTimeDisplay();
        setupClickListeners();
    }
    
    private void initViews() {
        btnVeryGood = findViewById(R.id.btn_very_good);
        btnGood = findViewById(R.id.btn_good);
        btnNormal = findViewById(R.id.btn_normal);
        btnBad = findViewById(R.id.btn_bad);
        btnVeryBad = findViewById(R.id.btn_very_bad);
        btnContinue = findViewById(R.id.btn_continue);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
    }
    
    private void setupDateTimeDisplay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        
        // Check if it's today
        Calendar today = Calendar.getInstance();
        boolean isToday = (today.get(Calendar.YEAR) == year && 
                          today.get(Calendar.MONTH) == month && 
                          today.get(Calendar.DAY_OF_MONTH) == day);
        
        // Format date
        String dateText;
        if (isToday) {
            SimpleDateFormat sdf = new SimpleDateFormat("'Today,' d MMMM", Locale.getDefault());
            dateText = sdf.format(calendar.getTime());
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
            dateText = sdf.format(calendar.getTime());
        }
        tvDate.setText(dateText);
        
        // Format time (current time)
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvTime.setText(timeSdf.format(Calendar.getInstance().getTime()));
    }
    
    private void setupClickListeners() {
        // Back button
        MaterialButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        
        // Mood selection buttons
        btnVeryGood.setOnClickListener(v -> selectMood(5, btnVeryGood));
        btnGood.setOnClickListener(v -> selectMood(4, btnGood));
        btnNormal.setOnClickListener(v -> selectMood(3, btnNormal));
        btnBad.setOnClickListener(v -> selectMood(2, btnBad));
        btnVeryBad.setOnClickListener(v -> selectMood(1, btnVeryBad));
        
        // Continue button
        btnContinue.setOnClickListener(v -> {
            if (selectedMood != -1) {
                openAddEntryActivity();
            }
        });
    }
    
    private void selectMood(int mood, ImageView button) {
        // Reset previous selection
        if (currentlySelected != null && currentlySelected != button) {
            animateScale(currentlySelected, 1.0f);
        }
        
        selectedMood = mood;
        currentlySelected = button;
        
        // Animate selected button
        animateScale(button, 1.3f);
        
        // Enable continue button
        btnContinue.setEnabled(true);
        btnContinue.setAlpha(1.0f);
    }
    
    private void animateScale(View view, float scale) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale);
        
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        
        scaleX.start();
        scaleY.start();
    }
    
    private void openAddEntryActivity() {
        Intent intent = new Intent(this, AddEntryActivity.class);
        intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, selectedMood);
        intent.putExtra(AddEntryActivity.EXTRA_DAY, day);
        intent.putExtra(AddEntryActivity.EXTRA_MONTH, month);
        intent.putExtra(AddEntryActivity.EXTRA_YEAR, year);
        intent.putExtra(AddEntryActivity.EXTRA_TIMESTAMP, entryTimestamp);
        startActivity(intent);
        finish();
    }
}
