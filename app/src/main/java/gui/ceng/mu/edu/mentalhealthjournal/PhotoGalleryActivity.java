package gui.ceng.mu.edu.mentalhealthjournal;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gui.ceng.mu.edu.mentalhealthjournal.data.entity.JournalEntryEntity;
import gui.ceng.mu.edu.mentalhealthjournal.data.repository.JournalRepository;

/**
 * Activity displaying all photos from journal entries in a grid layout.
 */
public class PhotoGalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoGridAdapter adapter;
    private List<String> photoPaths;
    private JournalRepository repository;
    private Handler mainHandler;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        repository = new JournalRepository(this);
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        loadPhotos();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        emptyText = findViewById(R.id.empty_text);
        recyclerView = findViewById(R.id.photos_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        photoPaths = new ArrayList<>();
        adapter = new PhotoGridAdapter(this, photoPaths);
        recyclerView.setAdapter(adapter);
    }

    private void loadPhotos() {
        repository.getAllEntries().observe(this, entities -> {
            photoPaths.clear();

            if (entities != null) {
                for (JournalEntryEntity entity : entities) {
                    String photoPath = entity.getPhotoPath();
                    if (photoPath != null && new File(photoPath).exists()) {
                        photoPaths.add(photoPath);
                    }
                }
            }

            if (photoPaths.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            adapter.notifyDataSetChanged();
        });
    }
}
