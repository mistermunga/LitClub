package com.litclub.ui.main.shared.view.subcomponent.library;

import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

// TODO [Important] API calls here
public class LibraryCore extends ScrollPane {

    private final VBox container;

    public LibraryCore() {
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

        showCurrentlyReading();
        showWantToRead();
        showReadSection();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    public void showCurrentlyReading() {
        VBox section = createSection(
                "Currently Reading",
                "Books you're actively reading",
                createMockBooks(3)
        );
        container.getChildren().add(section);
    }

    public void showWantToRead() {
        VBox section = createSection(
                "Want to Read",
                "Books on your reading list",
                createMockBooks(5)
        );
        container.getChildren().add(section);
    }

    public void showReadSection() {
        VBox section = createSection(
                "Read",
                "Books you've completed",
                createMockBooks(8)
        );
        container.getChildren().add(section);
    }

    private VBox createSection(String title, String subtitle, FlowPane booksPane) {
        VBox section = new VBox(15);
        section.getStyleClass().add("library-section");

        // Header
        VBox header = new VBox(5);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().addAll("section-subtitle");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Books container
        booksPane.getStyleClass().add("books-container");

        section.getChildren().addAll(header, booksPane);
        return section;
    }

    private FlowPane createMockBooks(int count) {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyleClass().add("books-container");

        for (int i = 0; i < count; i++) {
            VBox bookCard = createBookCard(
                    "Book Title " + (i + 1),
                    "Author Name",
                    "Genre"
            );
            flowPane.getChildren().add(bookCard);
        }

        return flowPane;
    }

    private VBox createBookCard(String title, String author, String genre) {
        VBox card = new VBox();
        card.getStyleClass().add("book-card");

        // Book cover placeholder
        VBox cover = new VBox();
        cover.getStyleClass().add("book-cover");

        Label coverText = new Label("📚");
        coverText.getStyleClass().add("book-cover-icon");
        cover.getChildren().add(coverText);

        // Book info
        VBox info = new VBox();
        info.getStyleClass().add("book-info");
        VBox.setVgrow(info, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("book-title");

        Label authorLabel = new Label(author);
        authorLabel.getStyleClass().add("book-author");

        Label genreLabel = new Label(genre);
        genreLabel.getStyleClass().add("book-genre");

        info.getChildren().addAll(titleLabel, authorLabel, genreLabel);

        card.getChildren().addAll(cover, info);

        return card;
    }
}