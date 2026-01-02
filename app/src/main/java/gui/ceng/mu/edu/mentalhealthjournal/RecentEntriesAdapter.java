package gui.ceng.mu.edu.mentalhealthjournal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentEntriesAdapter extends RecyclerView.Adapter<RecentEntriesAdapter.ViewHolder> {

    private List<JournalEntry> entries;

    public RecentEntriesAdapter(List<JournalEntry> entries) {
        this.entries = entries;
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
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageView moodIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.entry_title);
            date = itemView.findViewById(R.id.entry_date);
            moodIcon = itemView.findViewById(R.id.entry_mood_icon);
        }
    }
}
