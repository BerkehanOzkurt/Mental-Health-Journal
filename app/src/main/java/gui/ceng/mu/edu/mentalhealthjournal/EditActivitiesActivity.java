package gui.ceng.mu.edu.mentalhealthjournal;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import gui.ceng.mu.edu.mentalhealthjournal.data.repository.ActivityRepository;
import gui.ceng.mu.edu.mentalhealthjournal.model.Activity;

/**
 * Activity for viewing and editing custom activities.
 * Users can add custom activities, edit them, and swipe to delete.
 * Default activities cannot be deleted.
 */
public class EditActivitiesActivity extends AppCompatActivity {

    private static final String[] SUGGESTED_ICONS = {
            "🏃", "💼", "📖", "🎵", "🎮", "🍳", "🛒", "👥", "🧘", "🚶",
            "🎬", "👨‍👩‍👧‍👦", "⚽", "✈️", "📚", "🎨", "🌿", "😴", "💻", "🎯"
    };

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FloatingActionButton fabAddActivity;
    private ActivityListAdapter adapter;
    private ActivityRepository activityRepository;
    private List<Activity> activityList;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activities);

        activityRepository = new ActivityRepository(this);
        rootView = findViewById(android.R.id.content);

        setupToolbar();
        initViews();
        setupSwipeToDelete();
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

        fabAddActivity.setOnClickListener(v -> showAddActivityDialog());
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT
        ) {
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#F44336"));
            private final Drawable deleteIcon = ContextCompat.getDrawable(
                    EditActivitiesActivity.this, R.drawable.ic_delete);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Activity activity = activityList.get(position);

                if (activity.isDefault()) {
                    adapter.notifyItemChanged(position);
                    Toast.makeText(EditActivitiesActivity.this,
                            R.string.cannot_delete_default, Toast.LENGTH_SHORT).show();
                } else {
                    deleteActivityWithUndo(activity, position);
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < activityList.size()) {
                    Activity activity = activityList.get(position);
                    if (activity.isDefault()) {
                        return 0;
                    }
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                if (dX < 0) {
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconTop = itemView.getTop() + iconMargin;
                    int iconBottom = itemView.getBottom() - iconMargin;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.setTint(Color.WHITE);
                    deleteIcon.draw(c);
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void loadActivities() {
        activityList = new ArrayList<>(activityRepository.getAllActivities());

        adapter = new ActivityListAdapter(activityList, (activity, position) -> {
            if (activity.isDefault()) {
                Toast.makeText(EditActivitiesActivity.this,
                        "Cannot edit default activities", Toast.LENGTH_SHORT).show();
            } else {
                showEditActivityDialog(activity, position);
            }
        });

        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void showAddActivityDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_activity, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.activity_name_input);
        RecyclerView iconGrid = dialogView.findViewById(R.id.icon_grid);
        TextView selectedIconText = dialogView.findViewById(R.id.selected_icon);

        final String[] selectedIcon = {SUGGESTED_ICONS[0]};
        selectedIconText.setText(selectedIcon[0]);

        IconAdapter iconAdapter = new IconAdapter(SUGGESTED_ICONS, icon -> {
            selectedIcon[0] = icon;
            selectedIconText.setText(icon);
        });
        iconGrid.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 5));
        iconGrid.setAdapter(iconAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_custom_activity)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = nameInput.getText() != null ?
                            nameInput.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter an activity name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Activity newActivity = Activity.createCustom(name, selectedIcon[0], R.color.activity_custom);
                        activityRepository.addCustomActivity(newActivity);
                        activityList.add(newActivity);
                        adapter.notifyItemInserted(activityList.size() - 1);
                        updateEmptyState();
                        Toast.makeText(this, R.string.activity_added, Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, R.string.activity_already_exists, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditActivityDialog(Activity activity, int position) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_activity, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.activity_name_input);
        RecyclerView iconGrid = dialogView.findViewById(R.id.icon_grid);
        TextView selectedIconText = dialogView.findViewById(R.id.selected_icon);

        nameInput.setText(activity.getName());
        final String[] selectedIcon = {activity.getIcon()};
        selectedIconText.setText(selectedIcon[0]);

        IconAdapter iconAdapter = new IconAdapter(SUGGESTED_ICONS, icon -> {
            selectedIcon[0] = icon;
            selectedIconText.setText(icon);
        });
        iconGrid.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 5));
        iconGrid.setAdapter(iconAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_activity)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = nameInput.getText() != null ?
                            nameInput.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter an activity name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Activity updatedActivity = Activity.createCustom(name, selectedIcon[0], R.color.activity_custom);
                        activityRepository.updateCustomActivity(activity.getName(), updatedActivity);
                        activityList.set(position, updatedActivity);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Activity updated", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteActivityWithUndo(Activity activity, int position) {
        activityList.remove(position);
        adapter.notifyItemRemoved(position);
        updateEmptyState();

        Snackbar.make(rootView, R.string.activity_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    activityList.add(position, activity);
                    adapter.notifyItemInserted(position);
                    updateEmptyState();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            activityRepository.removeCustomActivity(activity.getName());
                        }
                    }
                })
                .show();
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

    // ===================== ADAPTERS =====================

    public static class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.ActivityViewHolder> {

        private final List<Activity> activities;
        private final OnActivityClickListener listener;

        public interface OnActivityClickListener {
            void onEditClick(Activity activity, int position);
        }

        public ActivityListAdapter(List<Activity> activities, OnActivityClickListener listener) {
            this.activities = activities;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_edit_activity, parent, false);
            return new ActivityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
            Activity activity = activities.get(position);
            holder.bind(activity, listener, position);
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        static class ActivityViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvEmoji;
            private final TextView tvName;
            private final MaterialButton btnEdit;
            private final MaterialButton btnDelete;
            private final TextView defaultBadge;

            ActivityViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tv_emoji);
                tvName = itemView.findViewById(R.id.tv_name);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                defaultBadge = itemView.findViewById(R.id.default_badge);
            }

            void bind(Activity activity, OnActivityClickListener listener, int position) {
                tvEmoji.setText(activity.getIcon());
                tvName.setText(activity.getName());

                if (activity.isDefault()) {
                    if (defaultBadge != null) {
                        defaultBadge.setVisibility(View.VISIBLE);
                    }
                    btnEdit.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.GONE);
                } else {
                    if (defaultBadge != null) {
                        defaultBadge.setVisibility(View.GONE);
                    }
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.GONE);
                    btnEdit.setOnClickListener(v -> listener.onEditClick(activity, position));
                }
            }
        }
    }

    public static class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

        private final String[] icons;
        private final OnIconSelectedListener listener;
        private int selectedPosition = 0;

        public interface OnIconSelectedListener {
            void onIconSelected(String icon);
        }

        public IconAdapter(String[] icons, OnIconSelectedListener listener) {
            this.icons = icons;
            this.listener = listener;
        }

        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(24);
            textView.setPadding(16, 16, 16, 16);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new IconViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            String icon = icons[position];
            holder.textView.setText(icon);
            holder.textView.setAlpha(position == selectedPosition ? 1.0f : 0.5f);
            holder.textView.setOnClickListener(v -> {
                int oldPos = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                listener.onIconSelected(icon);
            });
        }

        @Override
        public int getItemCount() {
            return icons.length;
        }

        static class IconViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            IconViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
