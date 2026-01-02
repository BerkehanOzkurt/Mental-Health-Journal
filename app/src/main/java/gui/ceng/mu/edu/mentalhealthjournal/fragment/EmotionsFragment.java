package gui.ceng.mu.edu.mentalhealthjournal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import gui.ceng.mu.edu.mentalhealthjournal.R;
import gui.ceng.mu.edu.mentalhealthjournal.adapter.ActivityItemAdapter;
import gui.ceng.mu.edu.mentalhealthjournal.model.ActivityItem;

import java.util.List;

/**
 * Fragment for displaying and selecting emotions.
 * Uses RecyclerView with GridLayoutManager to display emotion options.
 */
public class EmotionsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityItemAdapter adapter;
    private ImageButton btnToggle;
    private boolean isExpanded = true;

    private List<ActivityItem> emotions;

    public EmotionsFragment() {
        // Required empty public constructor
    }

    public static EmotionsFragment newInstance() {
        return new EmotionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        emotions = ActivityItem.getDefaultEmotions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emotions, container, false);

        recyclerView = view.findViewById(R.id.recycler_emotions);
        btnToggle = view.findViewById(R.id.btn_toggle_emotions);
        ImageButton btnAdd = view.findViewById(R.id.btn_add_emotion);

        // Setup RecyclerView with 5 columns grid (like in the screenshots)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        adapter = new ActivityItemAdapter(emotions);
        recyclerView.setAdapter(adapter);

        // Toggle expand/collapse
        btnToggle.setOnClickListener(v -> toggleExpand());

        // Add new emotion click
        btnAdd.setOnClickListener(v -> {
            // TODO: Implement add new emotion dialog
        });

        return view;
    }

    private void toggleExpand() {
        isExpanded = !isExpanded;
        if (isExpanded) {
            recyclerView.setVisibility(View.VISIBLE);
            btnToggle.setImageResource(R.drawable.ic_expand_less);
        } else {
            recyclerView.setVisibility(View.GONE);
            btnToggle.setImageResource(R.drawable.ic_expand_more);
        }
    }

    /**
     * Get list of selected emotion names
     */
    public List<String> getSelectedEmotions() {
        return adapter.getSelectedItemNames();
    }

    /**
     * Set selected emotions by their names
     */
    public void setSelectedEmotions(List<String> selectedNames) {
        adapter.setSelectedItems(selectedNames);
    }
}
