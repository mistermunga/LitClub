package com.litclub.ui.main.shared.view;

import com.litclub.construct.enums.ClubRole;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.ClubBookService;
import com.litclub.ui.main.shared.view.service.ClubService;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog.AddClubBookDialog;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog.AddDiscussionPromptDialog;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog.AddMeetingDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClubActions extends ScrollPane {

    private final ClubService clubService;
    private final ClubBookService clubBookService;
    private final AppSession session;

    private final VBox container;

    public ClubActions() {
        this.clubService = new ClubService();
        this.clubBookService = new ClubBookService();
        this.session = AppSession.getInstance();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().add("root");

        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        container = new VBox(30);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(40, 40, 40, 40));

        buildActionsPage();

        this.setContent(container);
    }

    private void buildActionsPage() {
        // Page header
        Label header = new Label("Club Actions");
        header.getStyleClass().add("section-title");
        header.setStyle("-fx-font-size: 32px;");

        ClubRole highestRole = session.getHighestRole();

        if (highestRole == null) {
            showNoPermissions();
            return;
        }

        container.getChildren().add(header);

        // Build sections based on role
        if (highestRole == ClubRole.MODERATOR) {
            container.getChildren().add(createModeratorSection());
        } else if (highestRole == ClubRole.OWNER) {
            container.getChildren().addAll(
                    createOwnerSection(),
                    createModeratorSection()
            );
        }
    }

    private void showNoPermissions() {
        VBox noPermBox = new VBox(20);
        noPermBox.setAlignment(Pos.CENTER);
        noPermBox.getStyleClass().add("card");
        noPermBox.setPadding(new Insets(60));
        noPermBox.setMaxWidth(600);

        Label icon = new Label("🔒");
        icon.setStyle("-fx-font-size: 48px;");

        Label message = new Label("No Actions Available");
        message.getStyleClass().add("section-title");

        Label subtitle = new Label("You don't have moderator or owner permissions in this club.");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        noPermBox.getChildren().addAll(icon, message, subtitle);
        container.getChildren().add(noPermBox);
    }

    private VBox createOwnerSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("card");
        section.setMaxWidth(800);

        Label sectionTitle = new Label("Owner Tools");
        sectionTitle.getStyleClass().add("section-subtitle");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Add Club Book
        VBox addBookBox = createActionCard(
                "📚",
                "Add Club Book",
                "Add a book from your currently reading list to the club library",
                this::handleAddClubBook
        );

        section.getChildren().addAll(sectionTitle, addBookBox);
        return section;
    }

    private VBox createModeratorSection() {
        VBox section = new VBox(20);
        section.getStyleClass().add("card");
        section.setMaxWidth(800);

        Label sectionTitle = new Label("Moderator Tools");
        sectionTitle.getStyleClass().add("section-subtitle");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Generate Invite
        VBox inviteBox = createInviteCard();

        // Create Discussion
        VBox discussionBox = createActionCard(
                "💬",
                "Create Discussion Prompt",
                "Start a new discussion topic for club members to respond to",
                this::handleCreateDiscussion
        );

        // Schedule Meeting
        VBox meetingBox = createMeetingCard();

        section.getChildren().addAll(sectionTitle, inviteBox, discussionBox, meetingBox);
        return section;
    }

    private VBox createActionCard(String emoji, String title, String description, Runnable action) {
        VBox card = new VBox(15);
        card.getStyleClass().add("container");
        card.setStyle("-fx-background-color: rgba(168, 181, 162, 0.05);");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 32px;");

        VBox textBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 16px;");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("text-muted");
        descLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, descLabel);

        topRow.getChildren().addAll(iconLabel, textBox);

        Button actionButton = new Button("Open");
        actionButton.getStyleClass().add("button-primary");
        actionButton.setOnAction(e -> action.run());

        card.getChildren().addAll(topRow, actionButton);
        return card;
    }

    private VBox createInviteCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("container");
        card.setStyle("-fx-background-color: rgba(168, 181, 162, 0.05);");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("🔗");
        iconLabel.setStyle("-fx-font-size: 32px;");

        VBox textBox = new VBox(5);
        Label titleLabel = new Label("Generate Invite Code");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 16px;");

        Label descLabel = new Label("Create a single-use invitation code for new members");
        descLabel.getStyleClass().add("text-muted");
        descLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, descLabel);

        topRow.getChildren().addAll(iconLabel, textBox);

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button generateButton = new Button("Generate Code");
        generateButton.getStyleClass().add("button-primary");

        Label codeLabel = new Label();
        codeLabel.getStyleClass().add("label");
        codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        generateButton.setOnAction(e -> {
            String code = clubService.generateInvite().invite();
            codeLabel.setText(code);
        });

        buttonRow.getChildren().addAll(generateButton, codeLabel);

        card.getChildren().addAll(topRow, buttonRow);
        return card;
    }

    private VBox createMeetingCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("container");
        card.setStyle("-fx-background-color: rgba(168, 181, 162, 0.05);");

        HBox topRow = new HBox(15);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("📅");
        iconLabel.setStyle("-fx-font-size: 32px;");

        VBox textBox = new VBox(5);
        Label titleLabel = new Label("Schedule Meeting");
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 16px;");

        Label descLabel = new Label("Schedule an online or in-person meeting for club members");
        descLabel.getStyleClass().add("text-muted");
        descLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, descLabel);

        topRow.getChildren().addAll(iconLabel, textBox);

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        Button onlineButton = new Button("Online Meeting");
        onlineButton.getStyleClass().add("button-primary");
        onlineButton.setOnAction(e -> handleCreateMeeting(true));

        Button inPersonButton = new Button("In-Person Meeting");
        inPersonButton.getStyleClass().add("secondary-button");
        inPersonButton.setOnAction(e -> handleCreateMeeting(false));

        buttonRow.getChildren().addAll(onlineButton, inPersonButton);

        card.getChildren().addAll(topRow, buttonRow);
        return card;
    }

    // ==================== EVENT HANDLERS ====================

    private void handleAddClubBook() {
        AddClubBookDialog dialog = new AddClubBookDialog();
        dialog.showAndWait().ifPresent(book -> {
            System.out.println("Added club book: " + book.getTitle());
        });
    }

    private void handleCreateDiscussion() {
        AddDiscussionPromptDialog dialog = new AddDiscussionPromptDialog();
        dialog.showAndWait().ifPresent(prompt -> {
            System.out.println("Created discussion prompt: " + prompt.getPrompt());
        });
    }

    private void handleCreateMeeting(boolean isOnline) {
        AddMeetingDialog dialog = new AddMeetingDialog(isOnline);
        dialog.showAndWait().ifPresent(meeting -> {
            System.out.println("Scheduled meeting: " + meeting.getTitle());
        });
    }
}