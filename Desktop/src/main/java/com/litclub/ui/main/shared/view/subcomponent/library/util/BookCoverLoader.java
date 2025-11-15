package com.litclub.ui.main.shared.view.subcomponent.library.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching book cover images.
 *
 * <p>Handles asynchronous image loading with fallback for failed loads.
 * Caches loaded images to avoid repeated network requests.</p>
 */
public class BookCoverLoader {

    private static final int COVER_WIDTH = 116;
    private static final int COVER_HEIGHT = 160;

    // Cache loaded images
    private final Map<String, Image> imageCache = new HashMap<>();

    /**
     * Load a book cover image from URL.
     * Returns null if loading fails.
     *
     * @param coverUrl the URL of the cover image
     * @return ImageView with the loaded image, or null if loading fails
     */
    public ImageView loadCover(String coverUrl) {
        if (coverUrl == null || coverUrl.trim().isEmpty()) {
            return null;
        }

        try {
            // Check cache first
            Image image;
            if (imageCache.containsKey(coverUrl)) {
                image = imageCache.get(coverUrl);
            } else {
                // Load image asynchronously
                image = new Image(
                        coverUrl,
                        COVER_WIDTH,
                        COVER_HEIGHT,
                        true,  // preserveRatio
                        true,  // smooth
                        true   // backgroundLoading
                );

                // Cache the image
                imageCache.put(coverUrl, image);
            }

            // Check if image loaded successfully
            if (image.isError()) {
                System.err.println("Failed to load cover: " + coverUrl);
                return null;
            }

            // Create ImageView
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(COVER_WIDTH);
            imageView.setFitHeight(COVER_HEIGHT);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            return imageView;

        } catch (Exception e) {
            System.err.println("Error loading cover from " + coverUrl + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Preload an image into the cache.
     * Useful for preloading covers before displaying them.
     *
     * @param coverUrl the URL to preload
     */
    public void preloadCover(String coverUrl) {
        if (coverUrl != null && !coverUrl.trim().isEmpty() && !imageCache.containsKey(coverUrl)) {
            try {
                Image image = new Image(
                        coverUrl,
                        COVER_WIDTH,
                        COVER_HEIGHT,
                        true,
                        true,
                        true
                );
                imageCache.put(coverUrl, image);
            } catch (Exception e) {
                System.err.println("Failed to preload cover: " + coverUrl);
            }
        }
    }

    /**
     * Clear the image cache.
     * Useful for memory management or when logging out.
     */
    public void clearCache() {
        imageCache.clear();
    }

    /**
     * Get the number of cached images.
     */
    public int getCacheSize() {
        return imageCache.size();
    }
}