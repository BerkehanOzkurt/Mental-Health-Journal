package gui.ceng.mu.edu.mentalhealthjournal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing and editing custom moods.
 * Users can add, edit, and delete mood entries.
 */
public class EditMoodsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FloatingActionButton fabAddMood;
    private MoodAdapter adapter;
    private List<MoodItem> moodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_moods);

        setupToolbar();
        initViews();
        loadMoods();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        fabAddMood = findViewById(R.id.fab_add_mood);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        fabAddMood.setOnClickListener(v -> {
            Toast.makeText(this, "Add mood feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMoods() {
        // Load default moods (in a real app, these would come from database/SharedPreferences)
        moodList = new ArrayList<>();
        moodList.add(new MoodItem(1, "Very Good", "😊", "#4CAF50"));
        moodList.add(new MoodItem(2, "Good", "🙂", "#8BC34A"));
        moodList.add(new MoodItem(3, "Neutral", "😐", "#FFC107"));
        moodList.add(new MoodItem(4, "Bad", "😟", "#FF9800"));
        moodList.add(new MoodItem(5, "Very Bad", "😢", "#F44336"));

        adapter = new MoodAdapter(moodList, new MoodAdapter.OnMoodClickListener() {
            @Override
            public void onEditClick(MoodItem mood) {
                Toast.makeText(EditMoodsActivity.this, 
                    "Edit " + mood.getName() + " - Coming soon!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(MoodItem mood) {
                Toast.makeText(EditMoodsActivity.this, 
                    "Cannot delete default moods", Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (moodList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    // Inner class for mood data
    public static class MoodItem {
        private int id;
        private String name;
        private String emoji;
        private String color;

        public MoodItem(int id, String name, String emoji, String color) {
            this.id = id;
            this.name = name;
            this.emoji = emoji;
            this.color = color;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmoji() { return emoji; }
        public String getColor() { return color; }
    }

    // Inner adapter class
    public static class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
        private List<MoodItem> moods;
        private OnMoodClickListener listener;

        public interface OnMoodClickListener {
            void onEditClick(MoodItem mood);
            void onDeleteClick(MoodItem mood);
        }

        public MoodAdapter(List<MoodItem> moods, OnMoodClickListener listener) {
            this.moods = moods;
            this.listener = listener;
        }

        @Override
        public MoodViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_edit_mood, parent, false);
            return new MoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MoodViewHolder holder, int position) {
            MoodItem mood = moods.get(position);
            holder.bind(mood, listener);
        }

        @Override
        public int getItemCount() {
            return moods.size();
        }

        public static class MoodViewHolder extends RecyclerView.ViewHolder {
            private TextView tvEmoji;
            private TextView tvName;
            private View colorIndicator;
            private com.google.android.material.button.MaterialButton btnEdit;
            private com.google.android.material.button.MaterialButton btnDelete;

            public MoodViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tv_emoji);
                tvName = itemView.findViewById(R.id.tv_name);
                colorIndicator = itemView.findViewById(R.id.color_indicator);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }

            public void bind(MoodItem mood, OnMoodClickListener listener) {
                tvEmoji.setText(mood.getEmoji());
                tvName.setText(mood.getName());
                colorIndicator.setBackgroundColor(android.graphics.Color.parseColor(mood.getColor()));
                
                btnEdit.setOnClickListener(v -> listener.onEditClick(mood));
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(mood));
            }
        }
    }
}
