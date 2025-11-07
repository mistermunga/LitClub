package com.litclub.persistence.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.litclub.client.api.ApiClient;
import com.litclub.construct.*;
import com.litclub.construct.enums.BookStatus;
import com.litclub.construct.interfaces.PageResponse;
import com.litclub.construct.interfaces.library.BookAddRequest;
import com.litclub.construct.interfaces.library.BookWithStatus;
import com.litclub.construct.interfaces.library.ReviewRequest;
import com.litclub.construct.interfaces.library.UserLibrary;
import com.litclub.construct.interfaces.library.book.BookSearchRequest;
import com.litclub.construct.interfaces.note.NoteCreateRequest;
import com.litclub.construct.interfaces.user.UserRecord;
import com.litclub.construct.interfaces.user.UserRegistrationRecord;
import com.litclub.construct.interfaces.user.UserLoginRecord;
import com.litclub.construct.interfaces.auth.AuthResponse;
import com.litclub.persistence.cache.CacheManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository managing library-related data: users, books, reviews, and personal notes.
 *
 * <p>This repository owns all ObservableLists for library domain objects and coordinates
 * between the API client and local cache. All operations return CompletableFuture for
 * async handling with UI loading states.</p>
 *
 * <p><strong>Thread Safety:</strong> All ObservableList modifications happen on JavaFX
 * Application Thread via Platform.runLater().</p>
 */
public class LibraryRepository {

    private static LibraryRepository instance;

    private final ApiClient apiClient;
    private final CacheManager cacheManager;

    // Observable data stores
    private final ObservableList<Book> allBooks;
    private final ObservableList<Book> currentlyReading;
    private final ObservableList<Book> wantToRead;
    private final ObservableList<Book> finishedReading;
    private final ObservableList<Review> userReviews;
    private final ObservableList<Note> personalNotes;

    // Current userRecord context
    private UserRecord currentUser;

    private LibraryRepository() {
        this.apiClient = ApiClient.getInstance();
        this.cacheManager = CacheManager.getInstance();

        // Initialize observable lists
        this.allBooks = FXCollections.observableArrayList();
        this.currentlyReading = FXCollections.observableArrayList();
        this.wantToRead = FXCollections.observableArrayList();
        this.finishedReading = FXCollections.observableArrayList();
        this.userReviews = FXCollections.observableArrayList();
        this.personalNotes = FXCollections.observableArrayList();
    }

    public static synchronized LibraryRepository getInstance() {
        if (instance == null) {
            instance = new LibraryRepository();
        }
        return instance;
    }

    // ==================== AUTHENTICATION ====================

    /**
     * Registers a new userRecord account.
     *
     * @param registrationRecord userRecord registration details
     * @return CompletableFuture with AuthResponse containing token and userRecord data
     */
    public CompletableFuture<AuthResponse> register(UserRegistrationRecord registrationRecord) {
        return apiClient.post("/api/auth/register", registrationRecord, AuthResponse.class)
                .thenApply(authResponse -> {
                    // Set authentication token
                    apiClient.setAuthToken(authResponse.token(), authResponse.userRecord().userID());

                    // Store current userRecord
                    this.currentUser = authResponse.userRecord();

                    return authResponse;
                });
    }

    /**
     * Logs in an existing userRecord.
     *
     * @param loginRecord login credentials (username/email and password)
     * @return CompletableFuture with AuthResponse containing token and userRecord data
     */
    public CompletableFuture<AuthResponse> login(UserLoginRecord loginRecord) {
        return apiClient.post("/api/auth/login", loginRecord, AuthResponse.class)
                .thenApply(authResponse -> {
                    // Set authentication token
                    apiClient.setAuthToken(authResponse.token(), authResponse.userRecord().userID());

                    // Store current userRecord
                    this.currentUser = authResponse.userRecord();

                    return authResponse;
                });
    }

    /**
     * Logs out the current userRecord and clears all data.
     */
    public void logout() {
        apiClient.clearAuthToken();
        this.currentUser = null;
        clearAllData();
    }

    // ==================== USER LIBRARY ====================

    /**
     * Fetches the current userRecord's library from the API and populates observable lists.
     *
     * @param userID the userRecord's ID
     * @return CompletableFuture that completes when library is loaded
     */
    public CompletableFuture<Void> fetchUserLibrary(Long userID) {
        return apiClient.get("/api/users/" + userID + "/library", UserLibrary.class)
                .thenAccept(library -> {
                    Platform.runLater(() -> {
                        // Clear existing data
                        currentlyReading.clear();
                        wantToRead.clear();
                        finishedReading.clear();

                        // Populate categorized lists
                        currentlyReading.addAll(library.currentlyReading().stream()
                                .map(BookWithStatus::book)
                                .toList());
                        wantToRead.addAll(library.wantToRead().stream()
                                .map(BookWithStatus::book)
                                .toList());
                        finishedReading.addAll(library.read().stream()
                                .map(BookWithStatus::book)
                                .toList());

                        // Update all books collection (union of all categories)
                        allBooks.clear();
                        allBooks.addAll(currentlyReading);
                        allBooks.addAll(wantToRead);
                        allBooks.addAll(finishedReading);
                    });

                    // Cache books
                    cacheManager.saveBooks(new ArrayList<>(allBooks));
                });
    }

    /**
     * Adds a book to the userRecord's library.
     *
     * @param userID the userRecord's ID
     * @param bookAddRequest book details and initial status
     * @return CompletableFuture with the added book
     */
    public CompletableFuture<BookWithStatus> addBookToLibrary(Long userID, BookAddRequest bookAddRequest) {
        return apiClient.post("/api/users/" + userID + "/library", bookAddRequest, BookWithStatus.class)
                .thenApply(bookWithStatus -> {
                    Platform.runLater(() -> {
                        Book book = bookWithStatus.book();

                        // Add to appropriate list based on status
                        switch (bookWithStatus.status()) {
                            case READING -> currentlyReading.add(book);
                            case WANT_TO_READ -> wantToRead.add(book);
                            case READ -> finishedReading.add(book);
                        }

                        // Add to all books
                        if (!allBooks.contains(book)) {
                            allBooks.add(book);
                        }
                    });

                    // Update cache
                    cacheManager.saveBooks(new ArrayList<>(allBooks));

                    return bookWithStatus;
                });
    }

    /**
     * Updates a book's status in the userRecord's library.
     *
     * @param userID the userRecord's ID
     * @param bookID the book's ID
     * @param newStatus the new reading status
     * @return CompletableFuture with updated book
     */
    public CompletableFuture<BookWithStatus> updateBookStatus(Long userID, Long bookID, BookStatus newStatus) {
        return apiClient.put("/api/users/" + userID + "/library/" + bookID, newStatus, BookWithStatus.class)
                .thenApply(bookWithStatus -> {
                    Platform.runLater(() -> {
                        Book book = bookWithStatus.book();

                        // Remove from all lists
                        currentlyReading.remove(book);
                        wantToRead.remove(book);
                        finishedReading.remove(book);

                        // Add to new list
                        switch (newStatus) {
                            case READING -> currentlyReading.add(book);
                            case WANT_TO_READ -> wantToRead.add(book);
                            case READ -> finishedReading.add(book);
                        }
                    });

                    // Update cache
                    cacheManager.saveBooks(new ArrayList<>(allBooks));

                    return bookWithStatus;
                });
    }

    /**
     * Removes a book from the userRecord's library.
     *
     * @param userID the userRecord's ID
     * @param bookID the book's ID
     * @return CompletableFuture that completes when book is removed
     */
    public CompletableFuture<Void> removeBookFromLibrary(Long userID, Long bookID) {
        return apiClient.delete("/api/users/" + userID + "/library/" + bookID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        // Remove from all lists
                        allBooks.removeIf(book -> book.getBookID().equals(bookID));
                        currentlyReading.removeIf(book -> book.getBookID().equals(bookID));
                        wantToRead.removeIf(book -> book.getBookID().equals(bookID));
                        finishedReading.removeIf(book -> book.getBookID().equals(bookID));
                    });

                    // Update cache
                    cacheManager.saveBooks(new ArrayList<>(allBooks));
                });
    }

    // ==================== BOOKS ====================

    /**
     * Fetches all books from the API (fetching multiple pages to get as much as possible).
     *
     * @return CompletableFuture that completes when books are loaded
     */
    public CompletableFuture<Void> fetchAllBooks() {
        return fetchAllBooksRecursive(0, new ArrayList<>())
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        allBooks.clear();
                        allBooks.addAll(books);
                    });

                    cacheManager.saveBooks(books);
                });
    }

    /**
     * Recursively fetches all pages of books.
     */
    private CompletableFuture<List<Book>> fetchAllBooksRecursive(int page, List<Book> accumulator) {
        TypeReference<PageResponse<Book>> typeRef = new TypeReference<>() {};

        return apiClient.get("/api/books?page=" + page + "&size=100", typeRef)
                .thenCompose(pageResponse -> {
                    accumulator.addAll(pageResponse.getContent());

                    // If there are more pages, fetch the next one
                    if (pageResponse.hasNext()) {
                        return fetchAllBooksRecursive(page + 1, accumulator);
                    } else {
                        return CompletableFuture.completedFuture(accumulator);
                    }
                });
    }

    /**
     * Searches for books by title, author, or ISBN.
     *
     * @param searchRequest search criteria
     * @return CompletableFuture with list of matching books
     */
    public CompletableFuture<List<Book>> searchBooks(BookSearchRequest searchRequest) {
        TypeReference<List<Book>> typeRef = new TypeReference<>() {};

        return apiClient.post("/api/books/search", searchRequest, typeRef)
                .thenApply(books -> {
                    // Cache searched books
                    Platform.runLater(() -> {
                        for (Book book : books) {
                            if (!allBooks.contains(book)) {
                                allBooks.add(book);
                            }
                        }
                    });

                    return books;
                });
    }

    /**
     * Gets a single book by ID.
     *
     * @param bookID the book's ID
     * @return CompletableFuture with the book
     */
    public CompletableFuture<Book> getBook(Long bookID) {
        return apiClient.get("/api/books/" + bookID, Book.class)
                .thenApply(book -> {
                    Platform.runLater(() -> {
                        if (!allBooks.contains(book)) {
                            allBooks.add(book);
                        }
                    });

                    return book;
                });
    }

    // ==================== REVIEWS ====================

    /**
     * Fetches reviews for the current userRecord (fetching multiple pages).
     *
     * @param userID the userRecord's ID
     * @return CompletableFuture that completes when reviews are loaded
     */
    public CompletableFuture<Void> fetchUserReviews(Long userID) {
        return fetchUserReviewsRecursive(userID, 0, new ArrayList<>())
                .thenAccept(reviews -> {
                    Platform.runLater(() -> {
                        userReviews.clear();
                        userReviews.addAll(reviews);
                    });

                    cacheManager.saveReviews(reviews);
                });
    }

    /**
     * Recursively fetches all pages of userRecord reviews.
     */
    private CompletableFuture<List<Review>> fetchUserReviewsRecursive(Long userID, int page, List<Review> accumulator) {
        TypeReference<PageResponse<Review>> typeRef = new TypeReference<>() {};

        return apiClient.get("/api/users/" + userID + "/reviews?page=" + page + "&size=100", typeRef)
                .thenCompose(pageResponse -> {
                    accumulator.addAll(pageResponse.getContent());

                    if (pageResponse.hasNext()) {
                        return fetchUserReviewsRecursive(userID, page + 1, accumulator);
                    } else {
                        return CompletableFuture.completedFuture(accumulator);
                    }
                });
    }

    /**
     * Creates or updates a review for a book.
     *
     * @param userID the userRecord's ID
     * @param bookID the book's ID
     * @param reviewRequest review content and rating
     * @return CompletableFuture with the created/updated review
     */
    public CompletableFuture<Review> createOrUpdateReview(Long userID, Long bookID, ReviewRequest reviewRequest) {
        return apiClient.post("/api/users/" + userID + "/reviews?bookID=" + bookID, reviewRequest, Review.class)
                .thenApply(review -> {
                    Platform.runLater(() -> {
                        // Remove old review if exists
                        userReviews.removeIf(r -> r.getBook().getBookID().equals(bookID));
                        // Add new review
                        userReviews.add(review);
                    });

                    cacheManager.saveReviews(new ArrayList<>(userReviews));

                    return review;
                });
    }

    /**
     * Deletes a review.
     *
     * @param userID the userRecord's ID
     * @param bookID the book's ID
     * @return CompletableFuture that completes when review is deleted
     */
    public CompletableFuture<Void> deleteReview(Long userID, Long bookID) {
        return apiClient.delete("/api/users/" + userID + "/reviews/" + bookID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        userReviews.removeIf(r -> r.getBook().getBookID().equals(bookID));
                    });

                    cacheManager.saveReviews(new ArrayList<>(userReviews));
                });
    }

    // ==================== PERSONAL NOTES ====================

    /**
     * Fetches personal notes for the current userRecord.
     *
     * @param userID the userRecord's ID
     * @return CompletableFuture that completes when notes are loaded
     */
    public CompletableFuture<Void> fetchPersonalNotes(Long userID) {
        TypeReference<List<Note>> typeRef = new TypeReference<>() {};

        return apiClient.get("/api/users/" + userID + "/notes", typeRef)
                .thenAccept(notes -> {
                    Platform.runLater(() -> {
                        personalNotes.clear();
                        personalNotes.addAll(notes);
                    });

                    cacheManager.saveNotes(notes);
                });
    }

    /**
     * Creates a personal note.
     *
     * @param userID the userRecord's ID
     * @param noteRequest note content and metadata
     * @return CompletableFuture with the created note
     */
    public CompletableFuture<Note> createPersonalNote(Long userID, NoteCreateRequest noteRequest) {
        return apiClient.post("/api/users/" + userID + "/notes", noteRequest, Note.class)
                .thenApply(note -> {
                    Platform.runLater(() -> {
                        personalNotes.add(note);
                    });

                    cacheManager.saveNotes(new ArrayList<>(personalNotes));

                    return note;
                });
    }

    /**
     * Updates a personal note.
     *
     * @param userID the userRecord's ID
     * @param noteID the note's ID
     * @param content new note content
     * @return CompletableFuture with the updated note
     */
    public CompletableFuture<Note> updatePersonalNote(Long userID, Long noteID, String content) {
        return apiClient.put("/api/users/" + userID + "/notes/" + noteID, content, Note.class)
                .thenApply(note -> {
                    Platform.runLater(() -> {
                        // Replace old note with updated one
                        personalNotes.removeIf(n -> n.getNoteID().equals(noteID));
                        personalNotes.add(note);
                    });

                    cacheManager.saveNotes(new ArrayList<>(personalNotes));

                    return note;
                });
    }

    /**
     * Deletes a personal note.
     *
     * @param userID the userRecord's ID
     * @param noteID the note's ID
     * @return CompletableFuture that completes when note is deleted
     */
    public CompletableFuture<Void> deletePersonalNote(Long userID, Long noteID) {
        return apiClient.delete("/api/users/" + userID + "/notes/" + noteID)
                .thenAccept(v -> {
                    Platform.runLater(() -> {
                        personalNotes.removeIf(n -> n.getNoteID().equals(noteID));
                    });

                    cacheManager.saveNotes(new ArrayList<>(personalNotes));
                });
    }

    // ==================== GETTERS FOR OBSERVABLE LISTS ====================

    public ObservableList<Book> getAllBooks() {
        return FXCollections.unmodifiableObservableList(allBooks);
    }

    public ObservableList<Book> getCurrentlyReading() {
        return FXCollections.unmodifiableObservableList(currentlyReading);
    }

    public ObservableList<Book> getWantToRead() {
        return FXCollections.unmodifiableObservableList(wantToRead);
    }

    public ObservableList<Book> getFinishedReading() {
        return FXCollections.unmodifiableObservableList(finishedReading);
    }

    public ObservableList<Review> getUserReviews() {
        return FXCollections.unmodifiableObservableList(userReviews);
    }

    public ObservableList<Note> getPersonalNotes() {
        return FXCollections.unmodifiableObservableList(personalNotes);
    }

    public UserRecord getCurrentUser() {
        return currentUser;
    }

    // ==================== UTILITY ====================

    /**
     * Clears all data from memory and cache.
     */
    public void clearAllData() {
        Platform.runLater(() -> {
            allBooks.clear();
            currentlyReading.clear();
            wantToRead.clear();
            finishedReading.clear();
            userReviews.clear();
            personalNotes.clear();
        });

        cacheManager.clearCache();
    }
}