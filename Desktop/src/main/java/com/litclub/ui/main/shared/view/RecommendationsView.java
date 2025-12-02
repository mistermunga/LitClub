package com.litclub.ui.main.shared.view;

import com.litclub.construct.Book;
import com.litclub.construct.enums.BookStatus;
import com.litclub.construct.interfaces.library.BookAddRequest;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.RecommendationsService;
import com.litclub.ui.main.shared.view.subcomponent.library.util.BookCoverLoader;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class RecommendationsView extends ScrollPane {

    private final RecommendationsService recommendationsService;
    private final LibraryService libraryService;
    private final BookCoverLoader coverLoader;
    private final VBox container;

    public RecommendationsView() {
        this.recommendationsService = new RecommendationsService();
        this.libraryService = new LibraryService();
        this.coverLoader = new BookCoverLoader();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("root", "scroll-pane");

        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        container = new VBox(20);
        container.setPadding(new Insets(30, 40, 30, 40));
        container.getStyleClass().add("container");

        setupSmoothScrolling();

        // Show loading initially
        showLoading();

        // Load recommendations
        loadRecommendations();

        this.setContent(container);
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        this.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    private void loadRecommendations() {
        Long userID = AppSession.getInstance().getUserRecord().userID();

        recommendationsService.loadRecommendations(
                userID,
                this::displayRecommendations,
                this::showError
        );
    }

    private void showLoading() {
        container.getChildren().clear();

        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(100));

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        Label loadingLabel = new Label("Finding recommendations for you...");
        loadingLabel.getStyleClass().add("section-subtitle");

        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
        container.getChildren().add(loadingBox);
    }

    private void displayRecommendations() {
        Platform.runLater(() -> {
            container.getChildren().clear();

            // Header
            Label headerLabel = new Label("Recommended for You");
            headerLabel.getStyleClass().add("section-title");
            headerLabel.setStyle("-fx-font-size: 32px;");

            Label subtitleLabel = new Label("Books you might enjoy based on your reading history");
            subtitleLabel.getStyleClass().add("section-subtitle");

            container.getChildren().addAll(headerLabel, subtitleLabel);

            ObservableList<Book> recommendations = recommendationsService.getRecommendedBooks();

            if (recommendations.isEmpty()) {
                showEmptyState();
                return;
            }

            // Add book cards
            for (Book book : recommendations) {
                VBox bookCard = createBookCard(book);
                container.getChildren().add(bookCard);
            }
        });
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.getStyleClass().add("meeting-card"); // Reusing meeting card style
        card.setCursor(javafx.scene.Cursor.HAND);

        // Main content row with cover and info
        HBox contentRow = new HBox(20);
        contentRow.setAlignment(Pos.CENTER_LEFT);

        // Book cover
        VBox coverBox = createCoverBox(book);

        // Book info
        VBox infoBox = createInfoBox(book);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        contentRow.getChildren().addAll(coverBox, infoBox);
        card.getChildren().add(contentRow);

        // Click handler - add to Want to Read
        card.setOnMouseClicked(e -> handleAddToLibrary(book, card));

        return card;
    }

    private VBox createCoverBox(Book book) {
        VBox coverContainer = new VBox();
        coverContainer.getStyleClass().add("book-cover");
        coverContainer.setAlignment(Pos.CENTER);

        if (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty()) {
            ImageView coverImage = coverLoader.loadCover(book.getCoverUrl());
            if (coverImage != null) {
                coverContainer.getChildren().add(coverImage);
            } else {
                addDefaultCoverIcon(coverContainer);
            }
        } else {
            addDefaultCoverIcon(coverContainer);
        }

        return coverContainer;
    }

    private void addDefaultCoverIcon(VBox container) {
        Label coverIcon = new Label("📚");
        coverIcon.getStyleClass().add("book-cover-icon");
        container.getChildren().add(coverIcon);
    }

    private VBox createInfoBox(Book book) {
        VBox infoBox = new VBox(8);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        // Title
        Label titleLabel = new Label(book.getTitle());
        titleLabel.getStyleClass().add("meeting-title"); // Reusing meeting title style
        titleLabel.setWrapText(true);

        // Author
        String author = book.getPrimaryAuthor() != null ? book.getPrimaryAuthor() : "Unknown Author";
        Label authorLabel = new Label("by " + author);
        authorLabel.getStyleClass().add("meeting-time"); // Reusing meeting time style

        // Year if available
        if (book.getYear() != null) {
            Label yearLabel = new Label("📅 " + book.getYear().getYear());
            yearLabel.getStyleClass().add("meeting-location"); // Reusing meeting location style
            infoBox.getChildren().addAll(titleLabel, authorLabel, yearLabel);
        } else {
            infoBox.getChildren().addAll(titleLabel, authorLabel);
        }

        // Add hint
        Label hintLabel = new Label("Click to add to Want to Read");
        hintLabel.getStyleClass().add("text-muted");
        hintLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
        infoBox.getChildren().add(hintLabel);

        return infoBox;
    }

    private void showEmptyState() {
        VBox emptyBox = new VBox(20);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(80));

        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 48px;");

        Label emptyLabel = new Label("No recommendations available yet");
        emptyLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label("Keep reading and adding books to get personalized recommendations!");
        subtitleLabel.getStyleClass().add("text-muted");
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(400);
        subtitleLabel.setAlignment(Pos.CENTER);

        emptyBox.getChildren().addAll(icon, emptyLabel, subtitleLabel);
        container.getChildren().add(emptyBox);
    }

    private void showError(String errorMessage) {
        Platform.runLater(() -> {
            container.getChildren().clear();

            VBox errorBox = new VBox(20);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(100));

            Label errorIcon = new Label("⚠️");
            errorIcon.setStyle("-fx-font-size: 48px;");

            Label errorLabel = new Label("Failed to load recommendations");
            errorLabel.getStyleClass().add("section-title");

            Label errorDetails = new Label(errorMessage);
            errorDetails.getStyleClass().addAll("text-muted", "error-label");
            errorDetails.setWrapText(true);
            errorDetails.setMaxWidth(400);
            errorDetails.setAlignment(Pos.CENTER);

            errorBox.getChildren().addAll(errorIcon, errorLabel, errorDetails);
            container.getChildren().add(errorBox);
        });
    }

    private void handleAddToLibrary(Book book, VBox card) {
        // Disable card to prevent double-clicks
        card.setDisable(true);

        Long userID = AppSession.getInstance().getUserRecord().userID();

        BookAddRequest request = new BookAddRequest(
                book.getTitle(),
                book.getPrimaryAuthor(),
                book.getIsbn(),
                BookStatus.WANT_TO_READ
        );

        libraryService.addBook(
                userID,
                request,
                bookWithStatus -> {
                    Platform.runLater(() -> {
                        // Show success feedback
                        card.setStyle("-fx-background-color: rgba(95, 168, 86, 0.2);");
                        EventBus.getInstance().emit(EventType.PERSONAL_LIBRARY_UPDATED);

                        // Update hint label
                        VBox infoBox = (VBox) ((HBox) card.getChildren().getFirst()).getChildren().get(1);
                        Label hintLabel = (Label) infoBox.getChildren().getLast();
                        hintLabel.setText("✓ Added to Want to Read");
                        hintLabel.getStyleClass().clear();
                        hintLabel.getStyleClass().add("success-label");

                        // Keep disabled after success
                        System.out.println("Added to library: " + book.getTitle());
                    });
                },
                errorMessage -> {
                    Platform.runLater(() -> {
                        // Re-enable on error
                        card.setDisable(false);

                        // Show error feedback
                        card.setStyle("-fx-background-color: rgba(229, 115, 104, 0.2);");

                        // Update hint label
                        VBox infoBox = (VBox) ((HBox) card.getChildren().getFirst()).getChildren().get(1);
                        Label hintLabel = (Label) infoBox.getChildren().getLast();
                        hintLabel.setText("Failed to add: " + errorMessage);
                        hintLabel.getStyleClass().clear();
                        hintLabel.getStyleClass().add("error-label");

                        System.err.println("Failed to add book: " + errorMessage);
                    });
                }
        );
    }
}