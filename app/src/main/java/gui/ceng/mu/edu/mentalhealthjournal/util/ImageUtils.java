package gui.ceng.mu.edu.mentalhealthjournal.util;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import gui.ceng.mu.edu.mentalhealthjournal.R;

/**
 * Utility class for image loading operations using Glide.
 * Provides a centralized, efficient way to load images throughout the app.
 */
public final class ImageUtils {

    // Prevent instantiation
    private ImageUtils() {}

    /**
     * Load an image from a file path into an ImageView.
     * @param context Context
     * @param imagePath Absolute path to the image file
     * @param imageView Target ImageView
     */
    public static void loadImage(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(file)
                .apply(getDefaultOptions())
                .into(imageView);
    }

    /**
     * Load an image from a Uri into an ImageView.
     * @param context Context
     * @param uri Uri of the image
     * @param imageView Target ImageView
     */
    public static void loadImage(Context context, Uri uri, ImageView imageView) {
        if (uri == null) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(uri)
                .apply(getDefaultOptions())
                .into(imageView);
    }

    /**
     * Load a thumbnail image from a file path.
     * @param context Context
     * @param imagePath Absolute path to the image file
     * @param imageView Target ImageView
     * @param size Thumbnail size in pixels
     */
    public static void loadThumbnail(Context context, String imagePath, ImageView imageView, int size) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(file)
                .apply(getThumbnailOptions(size))
                .into(imageView);
    }

    /**
     * Load a grid thumbnail (square, center-cropped).
     * @param context Context
     * @param imagePath Absolute path to the image file
     * @param imageView Target ImageView
     */
    public static void loadGridThumbnail(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        File file = new File(imagePath);
        if (!file.exists()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
            return;
        }

        Glide.with(context)
                .load(file)
                .apply(getGridOptions())
                .into(imageView);
    }

    /**
     * Clear image and show placeholder.
     * @param imageView Target ImageView
     */
    public static void clearImage(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_image_placeholder);
    }

    /**
     * Clear Glide cache for a specific file.
     * @param context Context
     * @param imagePath Path to the image
     */
    public static void clearCache(Context context, String imagePath) {
        if (imagePath != null) {
            Glide.get(context).clearMemory();
        }
    }

    private static RequestOptions getDefaultOptions() {
        return new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder);
    }

    private static RequestOptions getThumbnailOptions(int size) {
        return new RequestOptions()
                .override(size, size)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder);
    }

    private static RequestOptions getGridOptions() {
        return new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder);
    }
}
