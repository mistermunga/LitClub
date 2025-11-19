package com.litclub.ui.main.shared.view.subcomponent.library.subview;

import com.litclub.construct.Book;
import com.litclub.construct.Review;
import com.litclub.construct.enums.BookStatus;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.ReviewService;
import com.litclub.ui.main.shared.view.subcomponent.library.dialog.AddReviewDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Focused view showing a single book with its details, reviews, and actions.
 * Allows users to change status, view book info, and add/view reviews.
 */
public class BookFocus extends ScrollPane {

    private final LibraryService libraryService;
    private final ReviewService reviewService;
    private final Runnable onBack;
    private final VBox container;
    private final HBox bookHeader;

    private Book currentBook;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy");

    public BookFocus(LibraryService libraryService, Runnable onBack, ReviewService reviewService) {
        this.libraryService = libraryService;
        this.reviewService = reviewService;
        this.onBack = onBack;

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("library-core", "scroll-pane");

        // Main container
        container = new VBox(30);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("container");

        // Book header (will be populated when book is loaded)
        bookHeader = new HBox(20);
        bookHeader.getStyleClass().add("card");
        bookHeader.setPadding(new Insets(24));
        bookHeader.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().add(bookHeader);

        // Scroll pane setup
        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        setupSmoothScrolling();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    /**
     * Load and display a book with its reviews.
     */
    public void loadBook(Book book) {
        this.currentBook = book;
        buildBookHeader();
        addReviewsSection();
    }

    private void buildBookHeader() {
        bookHeader.getChildren().clear();

        // Left side: Back button
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> onBack.run());

        // Center: Book details
        VBox bookDetails = createBookDetails();
        HBox.setHgrow(bookDetails, Priority.ALWAYS);

        bookHeader.getChildren().addAll(backButton, bookDetails);
    }

    private VBox createBookDetails() {
        VBox details = new VBox(12);
        details.getStyleClass().add("book-info");

        // Title
        Label titleLabel = new Label(currentBook.getTitle());
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 24px;");

        // Author
        Label authorLabel = new Label("by " + currentBook.getPrimaryAuthor());
        authorLabel.getStyleClass().add("section-subtitle");
        authorLabel.setStyle("-fx-font-size: 16px;");

        // Year & ISBN row
        HBox metadataRow = new HBox(20);
        metadataRow.setAlignment(Pos.CENTER_LEFT);

        if (currentBook.getYear() != null) {
            Label yearLabel = new Label("📅 " + currentBook.getYear().getYear());
            yearLabel.getStyleClass().add("text-muted");
            metadataRow.getChildren().add(yearLabel);
        }

        if (currentBook.getIsbn() != null && !currentBook.getIsbn().isEmpty()) {
            Label isbnLabel = new Label("ISBN: " + currentBook.getIsbn());
            isbnLabel.getStyleClass().add("text-muted");
            metadataRow.getChildren().add(isbnLabel);
        }

        // Status selector
        HBox statusRow = createStatusSelector();

        // Action buttons
        HBox actionRow = createActionButtons();

        details.getChildren().addAll(titleLabel, authorLabel, metadataRow, statusRow, actionRow);
        return details;
    }

    private HBox createStatusSelector() {
        HBox statusRow = new HBox(10);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("label");

        ComboBox<BookStatus> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(
                BookStatus.WANT_TO_READ,
                BookStatus.READING,
                BookStatus.READ,
                BookStatus.DNF
        );

        // Set current status
        BookStatus currentStatus = libraryService.getBookStatus(currentBook.getBookID());
        if (currentStatus != null) {
            statusComboBox.setValue(currentStatus);
        }

        statusComboBox.getStyleClass().add("text-input");
        statusComboBox.setOnAction(e -> handleStatusChange(statusComboBox.getValue()));

        statusRow.getChildren().addAll(statusLabel, statusComboBox);
        return statusRow;
    }

    private HBox createActionButtons() {
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button addReviewButton = new Button("📝 Add Review");
        addReviewButton.getStyleClass().add("button-primary");
        addReviewButton.setOnAction(e -> handleAddReview());

        Button removeButton = new Button("Remove from Library");
        removeButton.getStyleClass().add("secondary-button");
        removeButton.setOnAction(e -> handleRemoveBook());

        actionRow.getChildren().addAll(addReviewButton, removeButton);
        return actionRow;
    }

    private void addReviewsSection() {
        // Clear any existing content below header
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }

        VBox reviewsSection = new VBox(15);
        reviewsSection.getStyleClass().add("card");
        reviewsSection.setPadding(new Insets(24));

        Label reviewsHeader = new Label("Reviews");
        reviewsHeader.getStyleClass().add("section-title");

        // TODO: Load and display reviews here
        Label placeholder = new Label("Reviews will be displayed here");
        placeholder.getStyleClass().add("text-muted");
        placeholder.setStyle("-fx-font-style: italic;");

        reviewsSection.getChildren().addAll(reviewsHeader, placeholder);
        container.getChildren().add(reviewsSection);
    }

    // ==================== EVENT HANDLERS ====================

    private void handleStatusChange(BookStatus newStatus) {
        System.out.println("Changing status to: " + newStatus);
        // TODO: Call library service to update status
    }

    private void handleAddReview() {
        System.out.println("Add review for: " + currentBook.getTitle());
        AddReviewDialog reviewDialog = new AddReviewDialog(
                currentBook.getBookID(),
                currentBook.getTitle(),
                reviewService
        );
        reviewDialog.showAndWait();
    }

    private void handleRemoveBook() {
        System.out.println("Remove book: " + currentBook.getTitle());
        // TODO: Show confirmation dialog and remove book
    }

    /**
     * Get the currently displayed book.
     */
    public Book getCurrentBook() {
        return currentBook;
    }
}
