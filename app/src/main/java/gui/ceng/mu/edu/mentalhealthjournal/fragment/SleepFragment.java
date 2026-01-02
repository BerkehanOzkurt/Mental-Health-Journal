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
 * Fragment for displaying and selecting sleep quality options.
 * Uses RecyclerView with GridLayoutManager to display sleep options.
 */
public class SleepFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityItemAdapter adapter;
    private ImageButton btnToggle;
    private boolean isExpanded = true;

    private List<ActivityItem> sleepOptions;

    public SleepFragment() {
        // Required empty public constructor
    }

    public static SleepFragment newInstance() {
        return new SleepFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sleepOptions = ActivityItem.getDefaultSleepOptions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep, container, false);

        recyclerView = view.findViewById(R.id.recycler_sleep);
        btnToggle = view.findViewById(R.id.btn_toggle_sleep);
        ImageButton btnAdd = view.findViewById(R.id.btn_add_sleep);

        // Setup RecyclerView with 4 columns grid (like in the screenshots)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new ActivityItemAdapter(sleepOptions);
        recyclerView.setAdapter(adapter);

        // Toggle expand/collapse
        btnToggle.setOnClickListener(v -> toggleExpand());

        // Add new sleep option click
        btnAdd.setOnClickListener(v -> {
            // TODO: Implement add new sleep option dialog
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
     * Get list of selected sleep option names
     */
    public List<String> getSelectedSleepOptions() {
        return adapter.getSelectedItemNames();
    }

    /**
     * Set selected sleep options by their names
     */
    public void setSelectedSleepOptions(List<String> selectedNames) {
        adapter.setSelectedItems(selectedNames);
    }
}
