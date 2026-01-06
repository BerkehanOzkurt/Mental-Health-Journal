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
 * Activity for viewing and editing custom activities.
 * Users can add, edit, and delete activity entries.
 */
public class EditActivitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FloatingActionButton fabAddActivity;
    private ActivityAdapter adapter;
    private List<ActivityItem> activityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activities);

        setupToolbar();
        initViews();
        loadActivities();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        emptyView = findViewById(R.id.empty_view);
        fabAddActivity = findViewById(R.id.fab_add_activity);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        fabAddActivity.setOnClickListener(v -> {
            Toast.makeText(this, "Add activity feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadActivities() {
        // Load default activities (in a real app, these would come from database/SharedPreferences)
        activityList = new ArrayList<>();
        activityList.add(new ActivityItem(1, "Work", "💼", "work"));
        activityList.add(new ActivityItem(2, "Exercise", "🏃", "exercise"));
        activityList.add(new ActivityItem(3, "Family", "👨‍👩‍👧", "family"));
        activityList.add(new ActivityItem(4, "Friends", "👥", "friends"));
        activityList.add(new ActivityItem(5, "Reading", "📚", "reading"));
        activityList.add(new ActivityItem(6, "Gaming", "🎮", "gaming"));
        activityList.add(new ActivityItem(7, "Music", "🎵", "music"));
        activityList.add(new ActivityItem(8, "Cooking", "🍳", "cooking"));
        activityList.add(new ActivityItem(9, "Shopping", "🛒", "shopping"));
        activityList.add(new ActivityItem(10, "Travel", "✈️", "travel"));

        adapter = new ActivityAdapter(activityList, new ActivityAdapter.OnActivityClickListener() {
            @Override
            public void onEditClick(ActivityItem activity) {
                Toast.makeText(EditActivitiesActivity.this, 
                    "Edit " + activity.getName() + " - Coming soon!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(ActivityItem activity) {
                Toast.makeText(EditActivitiesActivity.this, 
                    "Cannot delete default activities", Toast.LENGTH_SHORT).show();
            }
        });
        
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (activityList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    // Inner class for activity data
    public static class ActivityItem {
        private int id;
        private String name;
        private String emoji;
        private String key;

        public ActivityItem(int id, String name, String emoji, String key) {
            this.id = id;
            this.name = name;
            this.emoji = emoji;
            this.key = key;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmoji() { return emoji; }
        public String getKey() { return key; }
    }

    // Inner adapter class
    public static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
        private List<ActivityItem> activities;
        private OnActivityClickListener listener;

        public interface OnActivityClickListener {
            void onEditClick(ActivityItem activity);
            void onDeleteClick(ActivityItem activity);
        }

        public ActivityAdapter(List<ActivityItem> activities, OnActivityClickListener listener) {
            this.activities = activities;
            this.listener = listener;
        }

        @Override
        public ActivityViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_edit_activity, parent, false);
            return new ActivityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityViewHolder holder, int position) {
            ActivityItem activity = activities.get(position);
            holder.bind(activity, listener);
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        public static class ActivityViewHolder extends RecyclerView.ViewHolder {
            private TextView tvEmoji;
            private TextView tvName;
            private com.google.android.material.button.MaterialButton btnEdit;
            private com.google.android.material.button.MaterialButton btnDelete;

            public ActivityViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tv_emoji);
                tvName = itemView.findViewById(R.id.tv_name);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }

            public void bind(ActivityItem activity, OnActivityClickListener listener) {
                tvEmoji.setText(activity.getEmoji());
                tvName.setText(activity.getName());
                
                btnEdit.setOnClickListener(v -> listener.onEditClick(activity));
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(activity));
            }
        }
    }
}
