package com.litclub.ui.main.shared.view.subcomponent.library;

import com.litclub.construct.Book;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.subcomponent.library.util.BookCoverLoader;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LibraryCore extends ScrollPane {

    private final LibraryService libraryService;
    private final VBox container;
    private final BookCoverLoader coverLoader;

    // Section containers
    private FlowPane currentlyReadingPane;
    private FlowPane wantToReadPane;
    private FlowPane readPane;

    // Filter and sort state
    private Predicate<Book> currentFilter = book -> true; // Show all by default
    private Comparator<Book> currentSort = Comparator.comparing(
            (Book b) -> b.getBookID() != null ? b.getBookID() : 0L
    ).reversed(); // Recently added by default

    // Loading state
    private StackPane loadingContainer;
    private ProgressIndicator loadingIndicator;
    private Label loadingLabel;

    public LibraryCore() {
        this.libraryService = new LibraryService();
        this.coverLoader = new BookCoverLoader();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("library-core", "scroll-pane");

        container = new VBox(30);
        container.setPadding(new Insets(20));
        container.setFillWidth(true);
        container.getStyleClass().add("container");

        this.setVvalue(0);
        this.setPannable(false);
        setupSmoothScrolling();

        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Show loading state initially
        showLoading();

        // Load data
        loadLibraryData();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    // ==================== DATA LOADING ====================

    private void loadLibraryData() {
        Long userID = AppSession.getInstance().getUserRecord().userID();

        libraryService.loadLibrary(
                userID,
                // Success
                this::onLibraryLoaded,
                // Error
                this::showError
        );
    }

    private void onLibraryLoaded() {
        // Hide loading indicator
        container.getChildren().clear();

        // Build sections
        buildLibrarySections();

        setupDataListeners();

        System.out.println("Library loaded successfully!");
    }

    private void buildLibrarySections() {
        // Create section panes
        currentlyReadingPane = new FlowPane();
        wantToReadPane = new FlowPane();
        readPane = new FlowPane();

        // Build and add sections
        VBox currentlyReadingSection = createSection(
                "Currently Reading",
                "Books you're actively reading",
                currentlyReadingPane
        );

        VBox wantToReadSection = createSection(
                "Want to Read",
                "Books on your reading list",
                wantToReadPane
        );

        VBox readSection = createSection(
                "Read",
                "Books you've completed",
                readPane
        );

        container.getChildren().addAll(
                currentlyReadingSection,
                wantToReadSection,
                readSection
        );

        // Populate sections with data
        refreshAllSections();
    }

    // ==================== DATA LISTENERS ====================

    private void setupDataListeners() {
        // Listen for changes in Currently Reading list
        libraryService.getCurrentlyReading().addListener(
                (ListChangeListener.Change<? extends Book> c) -> {
                    System.out.println("Currently Reading list changed!");
                    if (currentlyReadingPane != null) {
                        refreshCurrentlyReading();
                    }
                }
        );

        // Listen for changes in Want to Read list
        libraryService.getWantToRead().addListener(
                (ListChangeListener.Change<? extends Book> c) -> {
                    System.out.println("Want to Read list changed!");
                    if (wantToReadPane != null) {
                        refreshWantToRead();
                    }
                }
        );

        // Listen for changes in Finished Reading list
        libraryService.getFinishedReading().addListener(
                (ListChangeListener.Change<? extends Book> c) -> {
                    System.out.println("Finished Reading list changed!");
                    if (readPane != null) {
                        refreshRead();
                    }
                }
        );
    }

    // ==================== SECTION REFRESH ====================

    private void refreshAllSections() {
        refreshCurrentlyReading();
        refreshWantToRead();
        refreshRead();
    }

    private void refreshCurrentlyReading() {
        var books = libraryService.getCurrentlyReading().stream()
                .filter(currentFilter)
                .sorted(currentSort)
                .collect(Collectors.toList());

        populateSection(currentlyReadingPane, books);
    }

    private void refreshWantToRead() {
        var books = libraryService.getWantToRead().stream()
                .filter(currentFilter)
                .sorted(currentSort)
                .collect(Collectors.toList());

        populateSection(wantToReadPane, books);
    }

    private void refreshRead() {
        var books = libraryService.getFinishedReading().stream()
                .filter(currentFilter)
                .sorted(currentSort)
                .collect(Collectors.toList());

        populateSection(readPane, books);
    }

    private void populateSection(FlowPane pane, java.util.List<Book> books) {
        pane.getChildren().clear();

        if (books.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label("No books in this section");
            emptyLabel.getStyleClass().addAll("text-muted");
            emptyLabel.setStyle("-fx-font-style: italic; -fx-padding: 20px;");
            pane.getChildren().add(emptyLabel);
            return;
        }

        // Create book cards
        for (Book book : books) {
            VBox bookCard = createBookCard(book);
            pane.getChildren().add(bookCard);
        }
    }

    // ==================== UI BUILDERS ====================

    private VBox createSection(String title, String subtitle, FlowPane booksPane) {
        VBox section = new VBox(15);
        section.getStyleClass().add("library-section");

        // Header
        VBox header = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("section-subtitle");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Books container
        booksPane.getStyleClass().add("books-container");

        section.getChildren().addAll(header, booksPane);
        return section;
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox();
        card.getStyleClass().add("book-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Book cover
        VBox coverContainer = new VBox();
        coverContainer.getStyleClass().add("book-cover");
        coverContainer.setAlignment(Pos.CENTER);

        // Try to load cover image, fallback to icon
        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            ImageView coverImage = coverLoader.loadCover(book.getCoverUrl());
            if (coverImage != null) {
                coverContainer.getChildren().add(coverImage);
            } else {
                // Fallback to icon
                addDefaultCoverIcon(coverContainer);
            }
        } else {
            // No cover URL, use default icon
            addDefaultCoverIcon(coverContainer);
        }

        // Book info
        VBox info = new VBox();
        info.getStyleClass().add("book-info");
        VBox.setVgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("book-title");

        Label authorLabel = new Label(book.getPrimaryAuthor());
        authorLabel.getStyleClass().add("book-author");

        // Show year if available
        String yearText = book.getYear() != null
                ? String.valueOf(book.getYear().getYear())
                : "Year unknown";
        Label yearLabel = new Label(yearText);
        yearLabel.getStyleClass().add("book-genre"); // Reuse genre style

        info.getChildren().addAll(titleLabel, authorLabel, yearLabel);

        card.getChildren().addAll(coverContainer, info);

        // Click handler - could open book details dialog
        card.setOnMouseClicked(e -> handleBookClick(book));

        return card;
    }

    private void addDefaultCoverIcon(VBox container) {
        Label coverIcon = new Label("📚");
        coverIcon.getStyleClass().add("book-cover-icon");
        container.getChildren().add(coverIcon);
    }

    private void handleBookClick(Book book) {
        System.out.println("Clicked book: " + book.getTitle());
        // TODO: Open book details dialog or menu
        // Could show options: View Details, Change Status, Remove, Add Review, etc.
    }

    // ==================== LOADING & ERROR STATES ====================

    private void showLoading() {
        container.getChildren().clear();

        loadingContainer = new StackPane();
        loadingContainer.setAlignment(Pos.CENTER);
        loadingContainer.setPadding(new Insets(100));

        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        loadingLabel = new Label("Loading your library...");
        loadingLabel.getStyleClass().add("section-subtitle");

        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
        loadingContainer.getChildren().add(loadingBox);

        container.getChildren().add(loadingContainer);
    }

    private void showError(String errorMessage) {
        container.getChildren().clear();

        VBox errorBox = new VBox(20);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(100));

        Label errorIcon = new Label("⚠️");
        errorIcon.setStyle("-fx-font-size: 48px;");

        Label errorLabel = new Label("Failed to load library");
        errorLabel.getStyleClass().add("section-title");

        Label errorDetails = new Label(errorMessage);
        errorDetails.getStyleClass().addAll("text-muted", "error-label");
        errorDetails.setWrapText(true);
        errorDetails.setMaxWidth(400);
        errorDetails.setAlignment(Pos.CENTER);

        errorBox.getChildren().addAll(errorIcon, errorLabel, errorDetails);

        container.getChildren().add(errorBox);
    }

    // ==================== PUBLIC API (for LibraryControlBar) ====================

    /**
     * Apply filter predicate to all sections.
     */
    public void applyFilter(Predicate<Book> filterPredicate) {
        this.currentFilter = filterPredicate;
        refreshAllSections();
    }

    /**
     * Apply sort comparator to all sections.
     */
    public void applySort(Comparator<Book> sortComparator) {
        this.currentSort = sortComparator;
        refreshAllSections();
    }

    /**
     * Refresh all sections (useful after adding/removing books).
     */
    public void refresh() {
        refreshAllSections();
    }

    /**
     * Get the library service instance.
     */
    public LibraryService getLibraryService() {
        return libraryService;
    }
}