package gui.ceng.mu.edu.mentalhealthjournal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import gui.ceng.mu.edu.mentalhealthjournal.R;
import gui.ceng.mu.edu.mentalhealthjournal.model.ActivityItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying ActivityItem objects in a RecyclerView grid.
 * Supports multi-selection of items.
 */
public class ActivityItemAdapter extends RecyclerView.Adapter<ActivityItemAdapter.ViewHolder> {

    private List<ActivityItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ActivityItem item, int position);
    }

    public ActivityItemAdapter(List<ActivityItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityItem item = items.get(position);
        holder.bind(item, position, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<ActivityItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Get list of selected item names
     */
    public List<String> getSelectedItemNames() {
        List<String> selectedNames = new ArrayList<>();
        for (ActivityItem item : items) {
            if (item.isSelected()) {
                selectedNames.add(item.getName());
            }
        }
        return selectedNames;
    }

    /**
     * Set selected items by their names
     */
    public void setSelectedItems(List<String> selectedNames) {
        if (selectedNames == null) return;
        for (ActivityItem item : items) {
            item.setSelected(selectedNames.contains(item.getName()));
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView nameView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.item_icon);
            nameView = itemView.findViewById(R.id.item_name);
        }

        public void bind(ActivityItem item, int position, OnItemClickListener listener) {
            iconView.setImageResource(item.getIconResourceId());
            nameView.setText(item.getName());
            
            // Update selection state
            iconView.setSelected(item.isSelected());
            
            // Click listener
            itemView.setOnClickListener(v -> {
                item.toggleSelection();
                iconView.setSelected(item.isSelected());
                if (listener != null) {
                    listener.onItemClick(item, position);
                }
            });
        }
    }
}
