package gui.ceng.mu.edu.mentalhealthjournal;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentEntriesAdapter extends RecyclerView.Adapter<RecentEntriesAdapter.ViewHolder> {

    private List<JournalEntry> entries;
    private OnEntryActionListener listener;
    private OnItemClickListener itemClickListener;

    public interface OnEntryActionListener {
        void onEditEntry(JournalEntry entry);
        void onDeleteEntry(JournalEntry entry);
    }

    public interface OnItemClickListener {
        void onItemClick(JournalEntry entry);
    }

    public RecentEntriesAdapter(List<JournalEntry> entries, OnEntryActionListener listener) {
        this.entries = entries;
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * Update the entries list with new data and refresh the adapter
     * @param newEntries The new list of entries to display
     */
    public void updateEntries(List<JournalEntry> newEntries) {
        this.entries.clear();
        if (newEntries != null) {
            this.entries.addAll(newEntries);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry entry = entries.get(position);
        holder.title.setText(entry.getTitle());
        holder.date.setText(entry.getDate());
        holder.moodIcon.setImageResource(entry.getMoodIcon());
        holder.moodIcon.setBackgroundResource(entry.getMoodBackground());

        // Item click listener for viewing entry details
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(entry);
            }
        });

        holder.btnMore.setOnClickListener(v -> {
            ContextThemeWrapper wrapper = new ContextThemeWrapper(v.getContext(), R.style.CustomPopupMenu);
            PopupMenu popup = new PopupMenu(wrapper, v);
            popup.getMenuInflater().inflate(R.menu.menu_entry_options, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) listener.onEditEntry(entry);
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) listener.onDeleteEntry(entry);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageView moodIcon, btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.entry_title);
            date = itemView.findViewById(R.id.entry_date);
            moodIcon = itemView.findViewById(R.id.entry_mood_icon);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}
