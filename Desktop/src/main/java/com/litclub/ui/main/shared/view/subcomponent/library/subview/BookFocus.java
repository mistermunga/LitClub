package com.litclub.ui.main.shared.view.subcomponent.library.subview;

import com.litclub.construct.Book;
import com.litclub.construct.Review;
import com.litclub.construct.enums.BookStatus;
import com.litclub.construct.interfaces.review.LoadedReview;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.ReviewService;
import com.litclub.ui.main.shared.view.subcomponent.library.dialog.AddReviewDialog;
import com.litclub.ui.main.shared.view.subcomponent.library.dialog.subcomponent.StarRater;
import javafx.collections.ObservableList;
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
    private VBox reviewsSection;

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
        loadReviewsSection();
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

    private void loadReviewsSection() {
        // Clear any existing content below header
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }

        // Create reviews section with loading indicator
        reviewsSection = new VBox(15);
        reviewsSection.getStyleClass().add("card");
        reviewsSection.setPadding(new Insets(24));

        Label reviewsHeader = new Label("Reviews");
        reviewsHeader.getStyleClass().add("section-title");

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(20));

        reviewsSection.getChildren().addAll(reviewsHeader, loadingBox);
        container.getChildren().add(reviewsSection);

        // Fetch reviews asynchronously
        reviewService.fetchReviews(
                currentBook.getBookID(),
                this::displayReviews,
                this::displayReviewsError
        );
    }

    private void displayReviews() {
        // Clear loading indicator
        reviewsSection.getChildren().clear();

        Label reviewsHeader = new Label("Reviews");
        reviewsHeader.getStyleClass().add("section-title");
        reviewsSection.getChildren().add(reviewsHeader);

        ObservableList<LoadedReview> reviews = reviewService.getReviewList();

        if (reviews.isEmpty()) {
            Label noReviewsLabel = new Label("No reviews yet. Be the first to review this book!");
            noReviewsLabel.getStyleClass().add("text-muted");
            noReviewsLabel.setStyle("-fx-font-style: italic;");
            reviewsSection.getChildren().add(noReviewsLabel);
        } else {
            VBox reviewsList = new VBox(15);
            for (LoadedReview review : reviews) {
                reviewsList.getChildren().add(createReviewCard(review));
            }
            reviewsSection.getChildren().add(reviewsList);
        }
    }

    private void displayReviewsError(String errorMessage) {
        // Clear loading indicator
        reviewsSection.getChildren().clear();

        Label reviewsHeader = new Label("Reviews");
        reviewsHeader.getStyleClass().add("section-title");

        Label errorLabel = new Label("Failed to load reviews: " + errorMessage);
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);

        Button retryButton = new Button("Retry");
        retryButton.getStyleClass().add("secondary-button");
        retryButton.setOnAction(e -> loadReviewsSection());

        reviewsSection.getChildren().addAll(reviewsHeader, errorLabel, retryButton);
    }

    private VBox createReviewCard(LoadedReview loadedReview) {
        VBox reviewCard = new VBox(12);
        reviewCard.setPadding(new Insets(20, 24, 20, 24));
        reviewCard.getStyleClass().add("card");

        Review review = loadedReview.review();

        // Header: username and date
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label("by " + loadedReview.username());
        usernameLabel.getStyleClass().add("label");
        usernameLabel.setStyle("-fx-font-weight: bold;");

        if (review.getContent() != null) {
            Label dateLabel = new Label("• " + review.getCreatedAt().format(DATE_FORMATTER));
            dateLabel.getStyleClass().add("text-muted");
            headerRow.getChildren().addAll(usernameLabel, dateLabel);
        } else {
            headerRow.getChildren().add(usernameLabel);
        }

        // Star rating
        StarRater starRater = new StarRater(false);
        starRater.setRating(review.getRating());

        reviewCard.getChildren().addAll(headerRow, starRater);

        // Review text (if present)
        if (review.getContent() != null && !review.getContent().trim().isEmpty()) {
            Label reviewTextLabel = new Label(review.getContent());
            reviewTextLabel.setWrapText(true);
            reviewTextLabel.getStyleClass().add("label");
            reviewTextLabel.setStyle("-fx-padding: 10 0 0 0;");
            reviewCard.getChildren().add(reviewTextLabel);
        }

        return reviewCard;
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
        reviewDialog.showAndWait().ifPresent(result -> {
            // Reload reviews after adding a new one
            loadReviewsSection();
        });
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