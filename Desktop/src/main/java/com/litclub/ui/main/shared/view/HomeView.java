package com.litclub.ui.main.shared.view;

import com.litclub.SceneManager;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.persistence.repository.LibraryRepository;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.ContentArea;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

public class HomeView extends ScrollPane {

    private final boolean isPersonal;
    private final LibraryRepository libraryRepository;
    private final ClubRepository clubRepository;
    private final AppSession session;

    private final VBox container;

    public HomeView(boolean isPersonal) {
        this.isPersonal = isPersonal;
        this.libraryRepository = LibraryRepository.getInstance();
        this.clubRepository = ClubRepository.getInstance();
        this.session = AppSession.getInstance();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("root", "scroll-pane");

        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        this.setPannable(false);

        container = new VBox();
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(40);
        container.setPadding(new Insets(60, 40, 40, 40));

        buildHome();
        this.setContent(container);

        if (isPersonal) {
            EventBus.getInstance().on(EventType.PERSONAL_LIBRARY_UPDATED, this::buildPersonalHome);
            EventBus.getInstance().on(EventType.PERSONAL_NOTES_UPDATED, this::buildPersonalHome);
        } else {
            EventBus.getInstance().on(EventType.CLUB_BOOK_UPDATED, this::buildClubHome);
            EventBus.getInstance().on(EventType.CLUB_NOTES_UPDATED, this::buildClubHome);
            EventBus.getInstance().on(EventType.DISCUSSION_PROMPTS_UPDATED, this::buildClubHome);
            EventBus.getInstance().on(EventType.CLUB_MEETINGS_UPDATED, this::buildClubHome);
        }

        setupSmoothScrolling();
    }

    private void buildHome() {
        if (isPersonal) {
            buildPersonalHome();
        } else {
            buildClubHome();
        }
    }

    // ==================== PERSONAL HOME ====================

    private void buildPersonalHome() {
        // Header
        Label header = new Label("Welcome to Your Library");
        header.getStyleClass().add("section-title");
        header.setStyle("-fx-font-size: 32px;");

        // Stats section
        VBox statsSection = createPersonalStatsSection();

        // Hero buttons
        FlowPane heroButtons = createPersonalHeroButtons();

        container.getChildren().addAll(header, statsSection, heroButtons);
    }

    private VBox createPersonalStatsSection() {
        VBox statsBox = new VBox(12);
        statsBox.getStyleClass().add("card");
        statsBox.setMaxWidth(800);
        statsBox.setAlignment(Pos.CENTER);

        Label statsTitle = new Label("Your Reading Stats");
        statsTitle.getStyleClass().add("section-subtitle");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox statsRow = new HBox(40);
        statsRow.setAlignment(Pos.CENTER);

        int totalBooks = libraryRepository.getAllBooks().size();
        VBox totalBooksBox = createStatItem("📚", String.valueOf(totalBooks), "Total Books");

        int currentlyReading = libraryRepository.getCurrentlyReading().size();
        VBox currentlyReadingBox = createStatItem("📖", String.valueOf(currentlyReading), "Currently Reading");

        int personalNotes = libraryRepository.getPersonalNotes().size();
        VBox notesBox = createStatItem("📝", String.valueOf(personalNotes), "Notes Written");

        statsRow.getChildren().addAll(totalBooksBox, currentlyReadingBox, notesBox);
        statsBox.getChildren().addAll(statsTitle, statsRow);
        return statsBox;
    }

    private FlowPane createPersonalHeroButtons() {
        FlowPane buttonPane = new FlowPane();
        buttonPane.setHgap(20);
        buttonPane.setVgap(20);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setMaxWidth(900);

        Button libraryBtn = createHeroButton("📚", "My Library",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Library");
                });

        Button notesBtn = createHeroButton("📝", "My Notes",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Notes");
                });

        Button addBookBtn = createHeroButton("➕", "Add Book",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Library");
                });

        Button clubsBtn = createHeroButton("🌐", "All Clubs",
                () -> SceneManager.getInstance().showCrossRoads());

        buttonPane.getChildren().addAll(libraryBtn, notesBtn, addBookBtn, clubsBtn);
        return buttonPane;
    }

    private void buildClubHome() {
        String clubName = session.getCurrentClub() != null
                ? session.getCurrentClub().getClubName()
                : "Book Club";

        Label header = new Label(clubName);
        header.getStyleClass().add("section-title");
        header.setStyle("-fx-font-size: 32px;");

        String description = session.getCurrentClub() != null
                && session.getCurrentClub().getDescription() != null
                ? session.getCurrentClub().getDescription()
                : "";

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("section-subtitle");
        descLabel.setStyle("-fx-font-size: 16px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(800);
        descLabel.setAlignment(Pos.CENTER);

        VBox statsSection = createClubStatsSection();
        FlowPane heroButtons = createClubHeroButtons();

        container.getChildren().addAll(header, descLabel, statsSection, heroButtons);
    }

    private VBox createClubStatsSection() {
        VBox statsBox = new VBox(12);
        statsBox.getStyleClass().add("card");
        statsBox.setMaxWidth(800);
        statsBox.setAlignment(Pos.CENTER);

        Label statsTitle = new Label("Club Stats");
        statsTitle.getStyleClass().add("section-subtitle");
        statsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox statsRow = new HBox(40);
        statsRow.setAlignment(Pos.CENTER);

        int clubBooks = clubRepository.getClubBooks().size();
        VBox booksBox = createStatItem("📚", String.valueOf(clubBooks), "Club Books");

        int discussions = clubRepository.getDiscussions().size();
        VBox discussionsBox = createStatItem("💬", String.valueOf(discussions), "Discussions");

        int meetings = clubRepository.getMeetings().size();
        VBox meetingsBox = createStatItem("📅", String.valueOf(meetings), "Meetings");

        int yourNotes = clubRepository.getClubNotes().size();
        VBox notesBox = createStatItem("📝", String.valueOf(yourNotes), "Club Notes");

        statsRow.getChildren().addAll(booksBox, discussionsBox, meetingsBox, notesBox);
        statsBox.getChildren().addAll(statsTitle, statsRow);
        return statsBox;
    }

    private FlowPane createClubHeroButtons() {
        FlowPane buttonPane = new FlowPane();
        buttonPane.setHgap(20);
        buttonPane.setVgap(20);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setMaxWidth(900);

        Button discussionsBtn = createHeroButton("💬", "Discussions",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Discussion");
                });

        Button notesBtn = createHeroButton("📝", "Notes",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Notes");
                });

        Button meetingsBtn = createHeroButton("📅", "Meetings",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Meetings");
                });

        Button membersBtn = createHeroButton("👥", "Members",
                () -> {
                    ContentArea contentArea = findContentArea();
                    if (contentArea != null) contentArea.showView("Members");
                });

        buttonPane.getChildren().addAll(discussionsBtn, notesBtn, meetingsBtn, membersBtn);

        if (session.getHighestRole() != null) {
            Button actionsBtn = createHeroButton("⚙️", "Actions",
                    () -> {
                        ContentArea contentArea = findContentArea();
                        if (contentArea != null) contentArea.showView("Actions");
                    });
            buttonPane.getChildren().add(actionsBtn);
        }

        return buttonPane;
    }

    private VBox createStatItem(String emoji, String number, String label) {
        VBox statBox = new VBox(8);
        statBox.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("section-title");
        numberLabel.setStyle("-fx-font-size: 28px;");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("text-muted");

        statBox.getChildren().addAll(emojiLabel, numberLabel, textLabel);
        return statBox;
    }

    private Button createHeroButton(String emoji, String text, Runnable action) {
        Button btn = new Button();
        btn.getStyleClass().add("button-primary");
        btn.setPrefWidth(200);
        btn.setPrefHeight(120);
        btn.setCursor(javafx.scene.Cursor.HAND);

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        content.getChildren().addAll(emojiLabel, textLabel);
        btn.setGraphic(content);

        btn.setOnAction(_ -> action.run());

        return btn;
    }

    private ContentArea findContentArea() {
        javafx.scene.Parent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof ContentArea) {
                return (ContentArea) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        this.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }
}
