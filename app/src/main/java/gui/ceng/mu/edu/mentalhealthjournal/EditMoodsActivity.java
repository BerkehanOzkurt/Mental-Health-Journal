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

import gui.ceng.mu.edu.mentalhealthjournal.data.repository.MoodRepository;
import gui.ceng.mu.edu.mentalhealthjournal.model.Mood;

/**
 * Activity for viewing and editing custom moods.
 * Users can add custom moods, edit them, and swipe to delete.
 * Default moods cannot be deleted.
 */
public class EditMoodsActivity extends AppCompatActivity {

    private static final String[] SUGGESTED_EMOJIS = {
            "😊", "😢", "😡", "😴", "🤔", "💪", "❤️", "🌈", "⭐", "🔥",
            "😎", "🥳", "😇", "🤗", "😤", "🥺", "😌", "🤯", "💭", "🙃"
    };

    private RecyclerView recyclerView;
    private TextView emptyView;
    private FloatingActionButton fabAddMood;
    private MoodListAdapter adapter;
    private MoodRepository moodRepository;
    private List<Mood> moodList;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_moods);

        moodRepository = new MoodRepository(this);
        rootView = findViewById(android.R.id.content);

        setupToolbar();
        initViews();
        setupSwipeToDelete();
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

        fabAddMood.setOnClickListener(v -> showAddMoodDialog());
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT
        ) {
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#F44336"));
            private final Drawable deleteIcon = ContextCompat.getDrawable(
                    EditMoodsActivity.this, R.drawable.ic_delete);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't support drag and drop reordering
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Mood mood = moodList.get(position);

                if (mood.isDefault()) {
                    // Can't delete default moods - restore the item
                    adapter.notifyItemChanged(position);
                    Toast.makeText(EditMoodsActivity.this,
                            R.string.cannot_delete_default, Toast.LENGTH_SHORT).show();
                } else {
                    // Delete custom mood with undo option
                    deleteMoodWithUndo(mood, position);
                }
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < moodList.size()) {
                    Mood mood = moodList.get(position);
                    // Disable swipe for default moods
                    if (mood.isDefault()) {
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

                if (dX < 0) { // Swiping left
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

    private void loadMoods() {
        moodList = new ArrayList<>(moodRepository.getAllMoods());

        adapter = new MoodListAdapter(moodList, new MoodListAdapter.OnMoodClickListener() {
            @Override
            public void onEditClick(Mood mood, int position) {
                if (mood.isDefault()) {
                    Toast.makeText(EditMoodsActivity.this,
                            "Cannot edit default moods", Toast.LENGTH_SHORT).show();
                } else {
                    showEditMoodDialog(mood, position);
                }
            }
        });

        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void showAddMoodDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_mood, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.mood_name_input);
        RecyclerView emojiGrid = dialogView.findViewById(R.id.emoji_grid);
        TextView selectedEmojiText = dialogView.findViewById(R.id.selected_emoji);

        // Setup emoji grid
        final String[] selectedEmoji = {SUGGESTED_EMOJIS[0]};
        selectedEmojiText.setText(selectedEmoji[0]);

        EmojiAdapter emojiAdapter = new EmojiAdapter(SUGGESTED_EMOJIS, emoji -> {
            selectedEmoji[0] = emoji;
            selectedEmojiText.setText(emoji);
        });
        emojiGrid.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 5));
        emojiGrid.setAdapter(emojiAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.add_custom_mood)
                .setView(dialogView)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = nameInput.getText() != null ?
                            nameInput.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter a mood name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Mood newMood = Mood.createCustom(name, selectedEmoji[0], R.color.emotion_custom);
                        moodRepository.addCustomMood(newMood);
                        moodList.add(newMood);
                        adapter.notifyItemInserted(moodList.size() - 1);
                        updateEmptyState();
                        Toast.makeText(this, R.string.mood_added, Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, R.string.mood_already_exists, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditMoodDialog(Mood mood, int position) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_mood, null);

        TextInputEditText nameInput = dialogView.findViewById(R.id.mood_name_input);
        RecyclerView emojiGrid = dialogView.findViewById(R.id.emoji_grid);
        TextView selectedEmojiText = dialogView.findViewById(R.id.selected_emoji);

        // Pre-fill with current values
        nameInput.setText(mood.getName());
        final String[] selectedEmoji = {mood.getEmoji()};
        selectedEmojiText.setText(selectedEmoji[0]);

        EmojiAdapter emojiAdapter = new EmojiAdapter(SUGGESTED_EMOJIS, emoji -> {
            selectedEmoji[0] = emoji;
            selectedEmojiText.setText(emoji);
        });
        emojiGrid.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 5));
        emojiGrid.setAdapter(emojiAdapter);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.edit_mood)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = nameInput.getText() != null ?
                            nameInput.getText().toString().trim() : "";
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter a mood name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Mood updatedMood = Mood.createCustom(name, selectedEmoji[0], R.color.emotion_custom);
                        moodRepository.updateCustomMood(mood.getName(), updatedMood);
                        moodList.set(position, updatedMood);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteMoodWithUndo(Mood mood, int position) {
        // Remove from list and adapter
        moodList.remove(position);
        adapter.notifyItemRemoved(position);
        updateEmptyState();

        // Show snackbar with undo option
        Snackbar.make(rootView, R.string.mood_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    // Restore the mood
                    moodList.add(position, mood);
                    adapter.notifyItemInserted(position);
                    updateEmptyState();
                })
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != DISMISS_EVENT_ACTION) {
                            // User didn't undo, actually delete from repository
                            moodRepository.removeCustomMood(mood.getName());
                        }
                    }
                })
                .show();
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

    // ===================== ADAPTERS =====================

    /**
     * Adapter for displaying moods in the list.
     */
    public static class MoodListAdapter extends RecyclerView.Adapter<MoodListAdapter.MoodViewHolder> {

        private final List<Mood> moods;
        private final OnMoodClickListener listener;

        public interface OnMoodClickListener {
            void onEditClick(Mood mood, int position);
        }

        public MoodListAdapter(List<Mood> moods, OnMoodClickListener listener) {
            this.moods = moods;
            this.listener = listener;
        }

        @NonNull
        @Override
        public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_edit_mood, parent, false);
            return new MoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
            Mood mood = moods.get(position);
            holder.bind(mood, listener, position);
        }

        @Override
        public int getItemCount() {
            return moods.size();
        }

        static class MoodViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvEmoji;
            private final TextView tvName;
            private final View colorIndicator;
            private final MaterialButton btnEdit;
            private final MaterialButton btnDelete;
            private final TextView defaultBadge;

            MoodViewHolder(View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tv_emoji);
                tvName = itemView.findViewById(R.id.tv_name);
                colorIndicator = itemView.findViewById(R.id.color_indicator);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                defaultBadge = itemView.findViewById(R.id.default_badge);
            }

            void bind(Mood mood, OnMoodClickListener listener, int position) {
                tvEmoji.setText(mood.getEmoji());
                tvName.setText(mood.getName());

                // Set color indicator
                try {
                    int color = ContextCompat.getColor(itemView.getContext(), mood.getColorResId());
                    colorIndicator.setBackgroundColor(color);
                } catch (Exception e) {
                    colorIndicator.setBackgroundColor(Color.GRAY);
                }

                // Show/hide default badge and adjust buttons
                if (mood.isDefault()) {
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
                    btnDelete.setVisibility(View.GONE); // We use swipe to delete instead
                    btnEdit.setOnClickListener(v -> listener.onEditClick(mood, position));
                }
            }
        }
    }

    /**
     * Adapter for emoji selection grid.
     */
    public static class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {

        private final String[] emojis;
        private final OnEmojiSelectedListener listener;
        private int selectedPosition = 0;

        public interface OnEmojiSelectedListener {
            void onEmojiSelected(String emoji);
        }

        public EmojiAdapter(String[] emojis, OnEmojiSelectedListener listener) {
            this.emojis = emojis;
            this.listener = listener;
        }

        @NonNull
        @Override
        public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(24);
            textView.setPadding(16, 16, 16, 16);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new EmojiViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
            String emoji = emojis[position];
            holder.textView.setText(emoji);
            holder.textView.setAlpha(position == selectedPosition ? 1.0f : 0.5f);
            holder.textView.setOnClickListener(v -> {
                int oldPos = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                listener.onEmojiSelected(emoji);
            });
        }

        @Override
        public int getItemCount() {
            return emojis.length;
        }

        static class EmojiViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            EmojiViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
