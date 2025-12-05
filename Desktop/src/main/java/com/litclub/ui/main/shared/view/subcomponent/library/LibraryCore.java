package com.litclub.ui.main.shared.view.subcomponent.library;

import com.litclub.construct.Book;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.ReviewService;
import com.litclub.ui.main.shared.view.subcomponent.library.subview.BookFocus;
import com.litclub.ui.main.shared.view.subcomponent.library.subview.DefaultLibraryCore;
import javafx.scene.layout.StackPane;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Core container for library view that manages navigation between:
 * - DefaultLibraryCore: Grid view of all books organized by status
 * - BookFocus: Detailed view of a single book with reviews and actions
 */
public class LibraryCore extends StackPane {

    private final DefaultLibraryCore defaultLibraryCore;
    private final BookFocus bookFocus;

    public LibraryCore() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("library-core");

        // Create library service once
        LibraryService libraryService = new LibraryService();
        ReviewService reviewService = new ReviewService();

        // Create subviews
        defaultLibraryCore = new DefaultLibraryCore(this::navigateToBook);
        bookFocus = new BookFocus(libraryService, this::navigateBack, reviewService);

        // Add both to stack (only one visible at a time)
        this.getChildren().addAll(defaultLibraryCore, bookFocus);

        // Events
        EventBus.getInstance().on(EventBus.personalEvents(), this::refresh);

        // Show default view initially
        showDefaultView();
    }

    // ==================== NAVIGATION ====================

    /**
     * Navigate to focused view showing a specific book with reviews.
     */
    private void navigateToBook(Book book) {
        System.out.println("Navigating to book: " + book.getTitle());

        // Load the book into focus view
        bookFocus.loadBook(book);

        // Show focus view, hide default view
        defaultLibraryCore.setVisible(false);
        bookFocus.setVisible(true);
    }

    /**
     * Navigate back to default grid view.
     */
    private void navigateBack() {
        System.out.println("Navigating back to library grid");

        // Show default view, hide focus view
        bookFocus.setVisible(false);
        defaultLibraryCore.setVisible(true);

        // Refresh default view in case books changed
        defaultLibraryCore.refresh();
    }

    /**
     * Show default grid view.
     */
    private void showDefaultView() {
        defaultLibraryCore.setVisible(true);
        bookFocus.setVisible(false);
    }

    // ==================== PUBLIC API (for LibraryControlBar) ====================

    public void applyFilter(Predicate<Book> filterPredicate) {
        defaultLibraryCore.applyFilter(filterPredicate);
    }

    public void applySort(Comparator<Book> sortComparator) {
        defaultLibraryCore.applySort(sortComparator);
    }

    public void refresh() {
        defaultLibraryCore.refresh();
    }

    public LibraryService getLibraryService() {
        return defaultLibraryCore.getLibraryService();
    }

    public boolean isViewingFocusedBook() {
        return bookFocus.isVisible();
    }

    public void showBooksGrid() {
        navigateBack();
    }
}