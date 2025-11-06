package com.litclub.persistence;

import com.google.gson.*;
import com.litclub.construct.*;
import com.litclub.construct.simulacra.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CacheManager {

    private static CacheManager instance;
    private final Path cacheDir;
    private final Gson gson;

    private static final String BOOKS_FILE = "books.json";
    private static final String NOTES_FILE = "notes.json";
    private static final String MEETINGS_FILE = "meetings.json";
    private static final String REVIEWS_FILE = "reviews.json";
    private static final String PROMPTS_FILE = "prompts.json";
    private static final String REPLIES_FILE = "replies.json";

    private CacheManager() throws IOException {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            cacheDir = Paths.get(System.getenv("APPDATA"), "LitClub");
        } else if (os.contains("mac")) {
            cacheDir = Paths.get(userHome, "Library", "Application Support", "LitClub");
        } else {
            cacheDir = Paths.get(userHome, ".litclub");
        }

        Files.createDirectories(cacheDir);

        // Configure Gson with LocalDateTime support
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static CacheManager getInstance() {
        if (instance == null) {
            try {
                instance = new CacheManager();
            } catch (IOException e) {
                throw new RuntimeException("Unable to initialize CacheManager", e);
            }
        }
        return instance;
    }

    // ==================== BOOKS ====================

    public void saveBooks(List<Book> books) {
        try {
            String json = gson.toJson(books);
            Files.writeString(cacheDir.resolve(BOOKS_FILE), json);
        } catch (IOException e) {
            System.err.println("Failed to save books: " + e.getMessage());
        }
    }

    public ArrayList<Book> loadBooks() {
        Path booksFile = cacheDir.resolve(BOOKS_FILE);

        if (!Files.exists(booksFile)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(booksFile);
            Book[] booksArray = gson.fromJson(json, Book[].class);
            return booksArray != null ? new ArrayList<>(List.of(booksArray)) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load books: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== NOTES ====================

    public void saveNotes(List<Note> notes) {
        try {
            String json = gson.toJson(notes);
            Files.writeString(cacheDir.resolve(NOTES_FILE), json);
        } catch (IOException e) {
            System.err.println("Failed to save notes: " + e.getMessage());
        }
    }

    public ArrayList<Note> loadNotes() {
        Path notesFile = cacheDir.resolve(NOTES_FILE);

        if (!Files.exists(notesFile)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(notesFile);
            Note[] notesArray = gson.fromJson(json, Note[].class);
            return notesArray != null ? new ArrayList<>(List.of(notesArray)) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load notes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== MEETINGS ====================

    public void saveMeetings(List<MeetingRecord> meetings) {
        try {
            String json = gson.toJson(meetings);
            Files.writeString(cacheDir.resolve(MEETINGS_FILE), json);
        } catch (IOException e) {
            System.err.println("Failed to save meetings: " + e.getMessage());
        }
    }

    public ArrayList<MeetingRecord> loadMeetings() {
        Path meetingsFile = cacheDir.resolve(MEETINGS_FILE);

        if (!Files.exists(meetingsFile)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(meetingsFile);
            MeetingRecord[] meetingsArray = gson.fromJson(json, MeetingRecord[].class);
            return meetingsArray != null ? new ArrayList<>(List.of(meetingsArray)) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load meetings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== REVIEWS ====================

    public void saveReviews(List<Review> reviews) {
        try {
            String json = gson.toJson(reviews);
            Files.writeString(cacheDir.resolve(REVIEWS_FILE), json);
        } catch (IOException e) {
            System.err.println("Failed to save reviews: " + e.getMessage());
        }
    }

    public ArrayList<Review> loadReviews() {
        Path reviewsFile = cacheDir.resolve(REVIEWS_FILE);

        if (!Files.exists(reviewsFile)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(reviewsFile);
            Review[] reviewsArray = gson.fromJson(json, Review[].class);
            return reviewsArray != null ? new ArrayList<>(List.of(reviewsArray)) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load reviews: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // =================== DISCUSSION PROMPTS =======================
    public void savePrompts(List<DiscussionPromptRecord> prompts) {
        try {
            String json = gson.toJson(prompts);
            Files.writeString(cacheDir.resolve(PROMPTS_FILE), json);
        } catch (IOException e) {
            System.err.println("Failed to save prompts: " + e.getMessage());
        }
    }

    public ArrayList<DiscussionPromptRecord> loadPrompts() {
        Path promptsFile = cacheDir.resolve(PROMPTS_FILE);

        if (!Files.exists(promptsFile)) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(promptsFile);
            DiscussionPromptRecord[] promptRecords = gson.fromJson(json, DiscussionPromptRecord[].class);
            return promptRecords != null ? new ArrayList<>(List.of(promptRecords)) : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Failed to load prompts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ==================== REPLIES ====================

    public void saveReplies(List<Reply> replies) {
        try {
            String json = gson.toJson(replies);
            Files.writeString(cacheDir.resolve(REPLIES_FILE), json);
        }  catch (IOException e) {
            System.err.println("Failed to save replies: " + e.getMessage());
        }
    }

    public ArrayList<Reply> loadReplies() {
        Path repliesFile = cacheDir.resolve(REPLIES_FILE);

        if (!Files.exists(repliesFile)) {
            return new ArrayList<>();
        }

        String json = gson.toJson(repliesFile);
        Reply[] repliesArray = gson.fromJson(json, Reply[].class);
        return repliesArray != null ? new ArrayList<>(List.of(repliesArray)) : new ArrayList<>();
    }

    // ==================== UTILITY ====================

    /**
     * Clears all cached data. Useful for logout or reset.
     */
    public void clearCache() {
        try {
            Files.deleteIfExists(cacheDir.resolve(BOOKS_FILE));
            Files.deleteIfExists(cacheDir.resolve(NOTES_FILE));
            Files.deleteIfExists(cacheDir.resolve(MEETINGS_FILE));
            Files.deleteIfExists(cacheDir.resolve(REVIEWS_FILE));
            Files.deleteIfExists(cacheDir.resolve(PROMPTS_FILE));
            Files.deleteIfExists(cacheDir.resolve(REPLIES_FILE));
            System.out.println("Cache cleared successfully");
        } catch (IOException e) {
            System.err.println("Failed to clear cache: " + e.getMessage());
        }
    }

    /**
     * Returns the cache directory path for debugging.
     */
    public Path getCacheDir() {
        return cacheDir;
    }

    /**
     * Checks if any cache files exist.
     */
    public boolean hasCachedData() {
        return Files.exists(cacheDir.resolve(BOOKS_FILE)) ||
                Files.exists(cacheDir.resolve(NOTES_FILE)) ||
                Files.exists(cacheDir.resolve(MEETINGS_FILE)) ||
                Files.exists(cacheDir.resolve(REVIEWS_FILE)) ||
                Files.exists(cacheDir.resolve(PROMPTS_FILE)) ||
                Files.exists(cacheDir.resolve(REPLIES_FILE));
    }

    // ==================== GSON ADAPTERS ====================

    /**
     * Custom Gson adapter for LocalDateTime serialization/deserialization.
     */
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(FORMATTER));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
}