package gui.ceng.mu.edu.mentalhealthjournal;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity displaying help and FAQ information.
 */
public class HelpActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FaqAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setupToolbar();
        initViews();
        loadFaqs();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadFaqs() {
        List<FaqItem> faqList = new ArrayList<>();
        
        faqList.add(new FaqItem(
            "How do I add a new journal entry?",
            "Tap the + button on the home screen to start a new entry. Select your mood, add emotions, activities, notes, photos, and voice memos as needed."
        ));
        
        faqList.add(new FaqItem(
            "How is the Current Streak calculated?",
            "The current streak counts consecutive days you've made at least one journal entry, counting backwards from today. If you miss a day, the streak resets."
        ));
        
        faqList.add(new FaqItem(
            "Can I edit or delete an entry?",
            "Yes! Tap on any entry to view it, then use the edit or delete options available on the entry detail screen."
        ));
        
        faqList.add(new FaqItem(
            "How do I view my mood statistics?",
            "Navigate to the Stats tab in the bottom navigation to see your mood trends, most common emotions, and activity patterns over different time periods."
        ));
        
        faqList.add(new FaqItem(
            "How do I set up daily reminders?",
            "Go to Settings and enable Daily Reminders. You can customize the time and frequency of notifications."
        ));
        
        faqList.add(new FaqItem(
            "Is my data backed up?",
            "Your data is stored locally on your device. Use the Backup & Restore feature in the More menu to save your data to external storage."
        ));
        
        faqList.add(new FaqItem(
            "How do I protect my journal with a PIN?",
            "Go to Settings and enable PIN Lock. You'll be asked to create a 4-digit PIN that will be required each time you open the app."
        ));
        
        faqList.add(new FaqItem(
            "Can I add photos and voice memos?",
            "Yes! When creating or editing an entry, you can add photos from your gallery or camera, and record voice memos to capture your thoughts."
        ));
        
        faqList.add(new FaqItem(
            "How do I switch between light and dark themes?",
            "Go to Settings and select your preferred theme under Appearance. Changes will apply immediately."
        ));

        adapter = new FaqAdapter(faqList);
        recyclerView.setAdapter(adapter);
    }

    // Inner class for FAQ data
    public static class FaqItem {
        private String question;
        private String answer;
        private boolean isExpanded;

        public FaqItem(String question, String answer) {
            this.question = question;
            this.answer = answer;
            this.isExpanded = false;
        }

        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
        public boolean isExpanded() { return isExpanded; }
        public void setExpanded(boolean expanded) { isExpanded = expanded; }
    }

    // Inner adapter class
    public static class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {
        private List<FaqItem> faqs;

        public FaqAdapter(List<FaqItem> faqs) {
            this.faqs = faqs;
        }

        @Override
        public FaqViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_faq, parent, false);
            return new FaqViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FaqViewHolder holder, int position) {
            FaqItem faq = faqs.get(position);
            holder.bind(faq, () -> {
                faq.setExpanded(!faq.isExpanded());
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return faqs.size();
        }

        public interface OnExpandListener {
            void onExpand();
        }

        public static class FaqViewHolder extends RecyclerView.ViewHolder {
            private TextView tvQuestion;
            private TextView tvAnswer;
            private View expandIndicator;

            public FaqViewHolder(View itemView) {
                super(itemView);
                tvQuestion = itemView.findViewById(R.id.tv_question);
                tvAnswer = itemView.findViewById(R.id.tv_answer);
                expandIndicator = itemView.findViewById(R.id.expand_indicator);
            }

            public void bind(FaqItem faq, OnExpandListener listener) {
                tvQuestion.setText(faq.getQuestion());
                tvAnswer.setText(faq.getAnswer());
                tvAnswer.setVisibility(faq.isExpanded() ? View.VISIBLE : View.GONE);
                
                if (expandIndicator != null) {
                    expandIndicator.setRotation(faq.isExpanded() ? 180f : 0f);
                }
                
                itemView.setOnClickListener(v -> listener.onExpand());
            }
        }
    }
}
