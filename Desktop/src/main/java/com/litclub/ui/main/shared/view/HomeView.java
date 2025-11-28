package com.litclub.ui.main.shared.view;

import com.litclub.SceneManager;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.persistence.repository.LibraryRepository;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.ContentArea;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class HomeView extends VBox {

    private final boolean isPersonal;
    private final LibraryRepository libraryRepository;
    private final ClubRepository clubRepository;
    private final AppSession session;

    public HomeView(boolean isPersonal) {
        this.isPersonal = isPersonal;
        this.libraryRepository = LibraryRepository.getInstance();
        this.clubRepository = ClubRepository.getInstance();
        this.session = AppSession.getInstance();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");

        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(40);
        this.setPadding(new Insets(60, 40, 40, 40));

        buildHome();
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

        this.getChildren().addAll(header, statsSection, heroButtons);
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

        // Total books
        int totalBooks = libraryRepository.getAllBooks().size();
        VBox totalBooksBox = createStatItem("📚", String.valueOf(totalBooks), "Total Books");

        // Currently reading
        int currentlyReading = libraryRepository.getCurrentlyReading().size();
        VBox currentlyReadingBox = createStatItem("📖", String.valueOf(currentlyReading), "Currently Reading");

        // Personal notes
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

    // ==================== CLUB HOME ====================

    private void buildClubHome() {
        // Header with club name
        String clubName = session.getCurrentClub() != null
                ? session.getCurrentClub().getClubName()
                : "Book Club";

        Label header = new Label(clubName);
        header.getStyleClass().add("section-title");
        header.setStyle("-fx-font-size: 32px;");

        // Club description
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

        // Stats section
        VBox statsSection = createClubStatsSection();

        // Hero buttons
        FlowPane heroButtons = createClubHeroButtons();

        this.getChildren().addAll(header, descLabel, statsSection, heroButtons);
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

        // Club books
        int clubBooks = clubRepository.getClubBooks().size();
        VBox booksBox = createStatItem("📚", String.valueOf(clubBooks), "Club Books");

        // Discussions
        int discussions = clubRepository.getDiscussions().size();
        VBox discussionsBox = createStatItem("💬", String.valueOf(discussions), "Discussions");

        // Meetings
        int meetings = clubRepository.getMeetings().size();
        VBox meetingsBox = createStatItem("📅", String.valueOf(meetings), "Meetings");

        // Your notes
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

    // ==================== HELPER METHODS ====================

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
        // Walk up the scene graph to find ContentArea
        javafx.scene.Parent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof ContentArea) {
                return (ContentArea) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}