package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Book;
import com.litclub.construct.enums.BookStatus;
import com.litclub.construct.interfaces.library.BookAddRequest;
import com.litclub.construct.interfaces.library.BookWithStatus;
import com.litclub.construct.interfaces.library.UserLibrary;
import com.litclub.construct.interfaces.library.book.BookSearchRequest;
import com.litclub.persistence.repository.LibraryRepository;
import com.litclub.session.AppSession;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Consumer;

public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final AppSession session;

    public LibraryService() {
        this.libraryRepository = LibraryRepository.getInstance();
        this.session = AppSession.getInstance();
    }

    // ==================== DATA ACCESS (delegates to repository) ====================

    public ObservableList<Book> getCurrentlyReading() {
        return libraryRepository.getCurrentlyReading();
    }

    public ObservableList<Book> getWantToRead() {
        return libraryRepository.getWantToRead();
    }

    public ObservableList<Book> getFinishedReading() {
        return libraryRepository.getFinishedReading();
    }

    public ObservableList<Book> getAllBooks() {
        return libraryRepository.getAllBooks();
    }

    // ==================== OPERATIONS (with UI-friendly callbacks) ====================

    /**
     * Load user's library from API.
     *
     * @param userID the user's ID
     * @param onSuccess callback when library loads successfully
     * @param onError callback with user-friendly error message
     */
    public void loadLibrary(Long userID,
                            Runnable onSuccess,
                            Consumer<String> onError) {

        libraryRepository.fetchUserLibrary(userID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Library loaded successfully");
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to load library: " + errorMessage);
                        onError.accept("Failed to load library: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Add a new book to library.
     *
     * @param userID the user's ID
     * @param request book details and initial status
     * @param onSuccess callback with the added book
     * @param onError callback with user-friendly error message
     */
    public void addBook(Long userID,
                        BookAddRequest request,
                        Consumer<BookWithStatus> onSuccess,
                        Consumer<String> onError) {

        libraryRepository.addBookToLibrary(userID, request)
                .thenAccept(bookWithStatus -> {
                    Platform.runLater(() -> {
                        System.out.println("Book added: " + bookWithStatus.book().getTitle());
                        onSuccess.accept(bookWithStatus);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to add book: " + errorMessage);
                        onError.accept("Failed to add book: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Update a book's reading status.
     *
     * @param userID the user's ID
     * @param bookID the book's ID
     * @param newStatus the new reading status
     * @param onSuccess callback with the updated book
     * @param onError callback with user-friendly error message
     */
    public void updateBookStatus(Long userID,
                                 Long bookID,
                                 BookStatus newStatus,
                                 Consumer<BookWithStatus> onSuccess,
                                 Consumer<String> onError) {

        libraryRepository.updateBookStatus(userID, bookID, newStatus)
                .thenAccept(bookWithStatus -> {
                    Platform.runLater(() -> {
                        System.out.println("Book status updated: " + bookWithStatus.book().getTitle() + " -> " + newStatus);
                        onSuccess.accept(bookWithStatus);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to update book status: " + errorMessage);
                        onError.accept("Failed to update book status: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Remove book from library.
     *
     * @param userID the user's ID
     * @param bookID the book's ID
     * @param onSuccess callback when book is removed
     * @param onError callback with user-friendly error message
     */
    public void removeBook(Long userID,
                           Long bookID,
                           Runnable onSuccess,
                           Consumer<String> onError) {

        libraryRepository.removeBookFromLibrary(userID, bookID)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        System.out.println("Book removed from library");
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to remove book: " + errorMessage);
                        onError.accept("Failed to remove book: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Search for books by title, author, or ISBN.
     *
     * @param searchRequest search criteria
     * @param onSuccess callback with list of matching books
     * @param onError callback with user-friendly error message
     */
    public void searchBooks(BookSearchRequest searchRequest,
                            Consumer<List<Book>> onSuccess,
                            Consumer<String> onError) {

        libraryRepository.searchBooks(searchRequest)
                .thenAccept(books -> {
                    Platform.runLater(() -> {
                        System.out.println("Search returned " + books.size() + " results");
                        onSuccess.accept(books);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to search books: " + errorMessage);
                        onError.accept("Failed to search books: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Get a single book by ID.
     *
     * @param bookID the book's ID
     * @param onSuccess callback with the book
     * @param onError callback with user-friendly error message
     */
    public void getBook(Long bookID,
                        Consumer<Book> onSuccess,
                        Consumer<String> onError) {

        libraryRepository.getBook(bookID)
                .thenAccept(book -> {
                    Platform.runLater(() -> {
                        System.out.println("Book retrieved: " + book.getTitle());
                        onSuccess.accept(book);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to get book: " + errorMessage);
                        onError.accept("Failed to get book: " + errorMessage);
                    });
                    return null;
                });
    }

    /**
     * Refresh library data from server.
     *
     * @param userID the user's ID
     * @param onSuccess callback when refresh completes
     * @param onError callback with user-friendly error message
     */
    public void refreshLibrary(Long userID,
                               Runnable onSuccess,
                               Consumer<String> onError) {
        // Same as loadLibrary - just a semantic alias
        loadLibrary(userID, onSuccess, onError);
    }

    // ==================== UTILITY ====================

    /**
     * Get current user's ID from session.
     *
     * @return user ID or null if not logged in
     */
    public Long getCurrentUserId() {
        return session.getUserRecord() != null ? session.getUserRecord().userID() : null;
    }

    /**
     * Check if a book exists in any of the user's lists.
     *
     * @param bookID the book's ID to check
     * @return true if book is in user's library
     */
    public boolean isBookInLibrary(Long bookID) {
        return getAllBooks().stream()
                .anyMatch(book -> book.getBookID().equals(bookID));
    }

    /**
     * Get the status of a book in the library.
     *
     * @param bookID the book's ID
     * @return the book's status, or null if not in library
     */
    public BookStatus getBookStatus(Long bookID) {
        if (getCurrentlyReading().stream().anyMatch(b -> b.getBookID().equals(bookID))) {
            return BookStatus.READING;
        } else if (getWantToRead().stream().anyMatch(b -> b.getBookID().equals(bookID))) {
            return BookStatus.WANT_TO_READ;
        } else if (getFinishedReading().stream().anyMatch(b -> b.getBookID().equals(bookID))) {
            return BookStatus.READ;
        }
        return null;
    }

    /**
     * Get count of books in a specific category.
     *
     * @param status the status to count
     * @return number of books with that status
     */
    public int getBookCount(BookStatus status) {
        return switch (status) {
            case READING -> getCurrentlyReading().size();
            case WANT_TO_READ -> getWantToRead().size();
            case READ -> getFinishedReading().size();
            case DNF -> 0; // TODO: Implement DNF list when needed
        };
    }

    /**
     * Get total book count across all categories.
     *
     * @return total number of books in library
     */
    public int getTotalBookCount() {
        return getAllBooks().size();
    }

    public int getBooksReadThisYear() {
        UserLibrary userLibrary = libraryRepository.getUserLibrary();
        if (userLibrary == null || userLibrary.read() == null) {
            return 0;
        }
        return userLibrary.read().size();
    }
}