package com.litclub.persistence;

import com.litclub.session.AppSession;
import com.litclub.session.construct.Book;
import com.litclub.session.construct.MeetingRecord;
import com.litclub.session.construct.Note;
import com.litclub.session.construct.Review;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;

/**
 * Central data management singleton for the LitClub application.
 * Manages all entities in observable collections and handles persistence via CacheManager.
 * <p>
 * Compatible with MockEntityGenerator for prototype phase.
 * Ready for API integration - just swap mock data source with API calls.
 * </p>
 */
public class DataRepository {

    private static DataRepository instance;

    // Observable collections for JavaFX binding
    private final ObservableList<Book> books;
    private final ObservableList<Note> notes;
    private final ObservableList<MeetingRecord> meetings;
    private final ObservableList<Review> reviews;

    private DataRepository() {
        // Initialize observable collections
        books = FXCollections.observableArrayList();
        notes = FXCollections.observableArrayList();
        meetings = FXCollections.observableArrayList();
        reviews = FXCollections.observableArrayList();

        // Load cached data (empty on first run)
        loadFromCache();
    }

    public static DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    /**
     * Loads all data from cache into observable lists.
     */
    private void loadFromCache() {
        CacheManager cache = CacheManager.getInstance();

        books.addAll(cache.loadBooks());
        notes.addAll(cache.loadNotes());
        meetings.addAll(cache.loadMeetings());
        reviews.addAll(cache.loadReviews());

        System.out.println("Loaded from cache: " + books.size() + " books, " +
                notes.size() + " notes, " + meetings.size() + " meetings, " +
                reviews.size() + " reviews");
    }

    // ==================== BOOKS ====================

    public ObservableList<Book> getBooks() {
        return books;
    }

    public Book getBookById(int bookId) {
        return books.stream()
                .filter(b -> b.getBookID() == bookId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a book with ID already assigned (from MockEntityGenerator or API).
     */
    public void addBook(Book book) {
        if (book.getBookID() == 0) {
            System.err.println("Warning: Book added without ID assigned");
        }
        books.add(book);
        saveBooks();
    }

    public void updateBook(Book book) {
        Book existing = getBookById(book.getBookID());
        if (existing != null) {
            int index = books.indexOf(existing);
            books.set(index, book);
            saveBooks();
        }
    }

    public void deleteBook(int bookId) {
        books.removeIf(b -> b.getBookID() == bookId);
        saveBooks();
    }

    private void saveBooks() {
        CacheManager.getInstance().saveBooks(books);
    }

    // ==================== NOTES ====================

    public ObservableList<Note> getNotes() {
        return notes;
    }

    public Note getNoteById(int noteId) {
        return notes.stream()
                .filter(n -> n.getNoteID() == noteId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Adds a note with ID already assigned (from MockEntityGenerator or API).
     * Sets createdAt timestamp if not already set.
     */
    public void addNote(Note note) {
        if (note.getNoteID() == 0) {
            System.err.println("Warning: Note added without ID assigned");
        }
        if (note.getCreatedAt() == null) {
            note.setCreatedAt(LocalDateTime.now());
        }
        notes.add(note);
        saveNotes();
    }

    public void updateNote(Note note) {
        Note existing = getNoteById(note.getNoteID());
        if (existing != null) {
            int index = notes.indexOf(existing);
            notes.set(index, note);
            saveNotes();
        }
    }

    public void deleteNote(int noteId) {
        notes.removeIf(n -> n.getNoteID() == noteId);
        saveNotes();
    }

    private void saveNotes() {
        CacheManager.getInstance().saveNotes(notes);
    }

    // ==================== MEETINGS ====================

    public ObservableList<MeetingRecord> getMeetings() {
        return meetings;
    }

    public void addMeeting(MeetingRecord meeting) {
        meetings.add(meeting);
        saveMeetings();
    }

    public void deleteMeeting(String meetingName) {
        meetings.removeIf(m -> m.meetingName().equals(meetingName));
        saveMeetings();
    }

    private void saveMeetings() {
        CacheManager.getInstance().saveMeetings(meetings);
    }

    // ==================== REVIEWS ====================

    public ObservableList<Review> getReviews() {
        return reviews;
    }

    /**
     * Returns the current user's review for a specific book, if it exists.
     */
    public Review getUserReviewForBook(int bookId) {
        int currentUserId = getCurrentUserId();
        return reviews.stream()
                .filter(r -> r.getBookID() == bookId && r.getUserID() == currentUserId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Calculates average rating for a book (1-10 scale).
     */
    public double getAverageRatingForBook(int bookId) {
        return reviews.stream()
                .filter(r -> r.getBookID() == bookId)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Adds a review with ID already assigned (from MockEntityGenerator or API).
     * Sets createdAt timestamp if not already set.
     */
    public void addReview(Review review) {
        if (review.getReviewID() == 0) {
            System.err.println("Warning: Review added without ID assigned");
        }
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        reviews.add(review);
        saveReviews();
    }

    public void updateReview(Review review) {
        Review existing = reviews.stream()
                .filter(r -> r.getReviewID() == review.getReviewID())
                .findFirst()
                .orElse(null);

        if (existing != null) {
            int index = reviews.indexOf(existing);
            reviews.set(index, review);
            saveReviews();
        }
    }

    public void deleteReview(int reviewId) {
        reviews.removeIf(r -> r.getReviewID() == reviewId);
        saveReviews();
    }

    private void saveReviews() {
        CacheManager.getInstance().saveReviews(reviews);
    }

    // ==================== UTILITY ====================

    /**
     * Clears all data from memory and cache.
     * Useful for logout or reset.
     */
    public void clearAll() {
        books.clear();
        notes.clear();
        meetings.clear();
        reviews.clear();

        CacheManager.getInstance().clearCache();
    }

    /**
     * Refreshes data from cache (useful for testing).
     */
    public void reloadFromCache() {
        books.clear();
        notes.clear();
        meetings.clear();
        reviews.clear();

        loadFromCache();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Gets the current user's ID from AppSession.
     * Returns -1 if no user is logged in.
     */
    private int getCurrentUserId() {
        return AppSession.getInstance().getUserRecord() != null
                ? AppSession.getInstance().getUserRecord().userID()
                : -1;
    }

    /**
     * Gets the current club ID from AppSession.
     * Returns -1 if no club is active.
     */
    private int getCurrentClubId() {
        return AppSession.getInstance().getClubRecord() != null
                ? AppSession.getInstance().getClubRecord().clubID()
                : -1;
    }
}