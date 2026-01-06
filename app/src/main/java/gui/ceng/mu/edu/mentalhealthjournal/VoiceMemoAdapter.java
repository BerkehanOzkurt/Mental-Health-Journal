package gui.ceng.mu.edu.mentalhealthjournal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying voice memos in a list.
 */
public class VoiceMemoAdapter extends RecyclerView.Adapter<VoiceMemoAdapter.VoiceMemoViewHolder> {

    public interface OnPlayClickListener {
        void onPlayClick(int position);
    }

    private Context context;
    private List<VoiceMemosActivity.VoiceMemoItem> voiceMemos;
    private OnPlayClickListener listener;
    private int playingPosition = -1;

    public VoiceMemoAdapter(Context context, List<VoiceMemosActivity.VoiceMemoItem> voiceMemos, OnPlayClickListener listener) {
        this.context = context;
        this.voiceMemos = voiceMemos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoiceMemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_voice_memo, parent, false);
        return new VoiceMemoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceMemoViewHolder holder, int position) {
        VoiceMemosActivity.VoiceMemoItem item = voiceMemos.get(position);

        holder.dateText.setText(item.getDateStr());

        // Set mood icon
        int iconRes = getMoodIcon(item.getMoodLevel());
        int bgRes = getMoodBackground(item.getMoodLevel());
        holder.moodIcon.setImageResource(iconRes);
        holder.moodIcon.setBackgroundResource(bgRes);

        // Set play button state
        if (position == playingPosition) {
            holder.playButton.setText("⏹ Stop");
            holder.playButton.setIconResource(R.drawable.ic_mic);
        } else {
            holder.playButton.setText("▶ Play");
            holder.playButton.setIconResource(R.drawable.ic_mic);
        }

        holder.playButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlayClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return voiceMemos.size();
    }

    public void setPlayingPosition(int position) {
        int oldPosition = playingPosition;
        playingPosition = position;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    private int getMoodIcon(int moodLevel) {
        switch (moodLevel) {
            case 5: return R.drawable.face1;
            case 4: return R.drawable.face2;
            case 3: return R.drawable.face3;
            case 2: return R.drawable.face4;
            case 1: default: return R.drawable.face5;
        }
    }

    private int getMoodBackground(int moodLevel) {
        switch (moodLevel) {
            case 5: return R.drawable.emoji_background_very_good;
            case 4: return R.drawable.emoji_background_good;
            case 3: return R.drawable.emoji_background_normal;
            case 2: return R.drawable.emoji_background_bad;
            case 1: default: return R.drawable.emoji_background_very_bad;
        }
    }

    static class VoiceMemoViewHolder extends RecyclerView.ViewHolder {
        ImageView moodIcon;
        TextView dateText;
        MaterialButton playButton;

        VoiceMemoViewHolder(@NonNull View itemView) {
            super(itemView);
            moodIcon = itemView.findViewById(R.id.mood_icon);
            dateText = itemView.findViewById(R.id.date_text);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
