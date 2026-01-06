package gui.ceng.mu.edu.mentalhealthjournal;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import gui.ceng.mu.edu.mentalhealthjournal.util.DateUtils;

public class MoodSelectionActivity extends AppCompatActivity {

    public static final String EXTRA_DAY = "day", EXTRA_MONTH = "month", EXTRA_YEAR = "year";
    
    private ImageView btnVeryGood, btnGood, btnNormal, btnBad, btnVeryBad;
    private TextView tvDate, tvTime;
    private int selectedMood = -1, day, month, year, selectedHour, selectedMinute;
    private ImageView currentlySelected = null;
    private long entryTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);
        
        Calendar cal = Calendar.getInstance();
        day = getIntent().getIntExtra(EXTRA_DAY, cal.get(Calendar.DAY_OF_MONTH));
        month = getIntent().getIntExtra(EXTRA_MONTH, cal.get(Calendar.MONTH));
        year = getIntent().getIntExtra(EXTRA_YEAR, cal.get(Calendar.YEAR));
        selectedHour = cal.get(Calendar.HOUR_OF_DAY);
        selectedMinute = cal.get(Calendar.MINUTE);
        entryTimestamp = DateUtils.getCalendar(year, month, day, selectedHour, selectedMinute).getTimeInMillis();
        
        initViews();
    }
    
    private void initViews() {
        btnVeryGood = findViewById(R.id.btn_very_good); btnGood = findViewById(R.id.btn_good);
        btnNormal = findViewById(R.id.btn_normal); btnBad = findViewById(R.id.btn_bad); btnVeryBad = findViewById(R.id.btn_very_bad);
        tvDate = findViewById(R.id.tv_date); tvTime = findViewById(R.id.tv_time);
        
        updateDateDisplay(); updateTimeDisplay();
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.date_container).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.time_container).setOnClickListener(v -> showTimePicker());
        
        btnVeryGood.setOnClickListener(v -> selectMood(5, btnVeryGood));
        btnGood.setOnClickListener(v -> selectMood(4, btnGood));
        btnNormal.setOnClickListener(v -> selectMood(3, btnNormal));
        btnBad.setOnClickListener(v -> selectMood(2, btnBad));
        btnVeryBad.setOnClickListener(v -> selectMood(1, btnVeryBad));
        
        View btnContinue = findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(v -> { if (selectedMood != -1) openAddEntryActivity(); });
    }
    
    private void updateDateDisplay() {
        Calendar cal = DateUtils.getCalendar(year, month, day);
        String pattern = DateUtils.isToday(cal) ? "'Today,' d MMMM" : "EEEE, d MMMM";
        tvDate.setText(DateUtils.format(cal, pattern));
    }
    
    private void updateTimeDisplay() { tvTime.setText(DateUtils.formatTime(selectedHour, selectedMinute)); }
    
    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this, (v, y, m, d) -> {
            if (DateUtils.isFuture(DateUtils.getCalendar(y, m, d))) { Toast.makeText(this, "Cannot select future dates", Toast.LENGTH_SHORT).show(); return; }
            year = y; month = m; day = d; updateDateDisplay(); updateTimestamp();
        }, year, month, day);
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }
    
    private void showTimePicker() {
        new TimePickerDialog(this, (v, h, m) -> { selectedHour = h; selectedMinute = m; updateTimeDisplay(); updateTimestamp(); }, selectedHour, selectedMinute, true).show();
    }
    
    private void updateTimestamp() { entryTimestamp = DateUtils.getCalendar(year, month, day, selectedHour, selectedMinute).getTimeInMillis(); }
    
    private void selectMood(int mood, ImageView btn) {
        if (currentlySelected != null && currentlySelected != btn) animateScale(currentlySelected, 1.0f);
        selectedMood = mood; currentlySelected = btn;
        animateScale(btn, 1.15f);
        findViewById(R.id.btn_continue).setEnabled(true);
        findViewById(R.id.btn_continue).setAlpha(1.0f);
    }
    
    private void animateScale(View v, float scale) {
        ObjectAnimator.ofFloat(v, "scaleX", scale).setDuration(200);
        ObjectAnimator.ofFloat(v, "scaleY", scale).setDuration(200);
        ObjectAnimator sx = ObjectAnimator.ofFloat(v, "scaleX", scale); sx.setDuration(200); sx.setInterpolator(new AccelerateDecelerateInterpolator()); sx.start();
        ObjectAnimator sy = ObjectAnimator.ofFloat(v, "scaleY", scale); sy.setDuration(200); sy.setInterpolator(new AccelerateDecelerateInterpolator()); sy.start();
    }
    
    private void openAddEntryActivity() {
        Intent intent = new Intent(this, AddEntryActivity.class);
        intent.putExtra(AddEntryActivity.EXTRA_MOOD_LEVEL, selectedMood);
        intent.putExtra(AddEntryActivity.EXTRA_DAY, day);
        intent.putExtra(AddEntryActivity.EXTRA_MONTH, month);
        intent.putExtra(AddEntryActivity.EXTRA_YEAR, year);
        intent.putExtra(AddEntryActivity.EXTRA_TIMESTAMP, entryTimestamp);
        startActivity(intent); finish();
    }
}
