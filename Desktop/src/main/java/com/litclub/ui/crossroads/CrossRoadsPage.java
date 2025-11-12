package com.litclub.ui.crossroads;

import com.litclub.SceneManager;
import com.litclub.construct.Club;
import com.litclub.construct.Meeting;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.crossroads.service.CrossRoadsService;
import com.litclub.ui.crossroads.subcomponents.MeetingsIsland;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CrossRoads Page - Main dashboard showing user's clubs, meetings, and personal card.
 *
 * <p>Layout:
 * <ul>
 *   <li>Header bar with user greeting and logout</li>
 *   <li>Meetings island showing upcoming meetings</li>
 *   <li>"Me" card for personal library/settings</li>
 *   <li>Grid of club cards</li>
 *   <li>Create club button (if allowed)</li>
 * </ul>
 */
public class CrossRoadsPage extends BorderPane {

    private final CrossRoadsService service;
    private final ThemeManager themeManager;

    // UI Components
    private VBox contentContainer;
    private MeetingsIsland meetingsIsland;
    private FlowPane clubsGrid;
    private Button createClubButton;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    public CrossRoadsPage() {
        this.service = new CrossRoadsService();
        this.themeManager = ThemeManager.getInstance();

        themeManager.registerComponent(this);
        this.getStyleClass().add("root");

        initializeUI();
        loadData();
    }

    // ==================== INITIALIZATION ====================

    private void initializeUI() {
        // Header
        setTop(createHeaderBar());

        // Main content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        contentContainer = new VBox(30);
        contentContainer.setPadding(new Insets(40));
        contentContainer.setAlignment(Pos.TOP_CENTER);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        VBox loadingBox = new VBox(20, loadingIndicator, new Label("Loading..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(100));

        contentContainer.getChildren().add(loadingBox);

        scrollPane.setContent(contentContainer);
        setCenter(scrollPane);

        // Status label (hidden by default)
        statusLabel = new Label();
        statusLabel.setVisible(false);
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setPadding(new Insets(10));
        setBottom(statusLabel);
    }

    private HBox createHeaderBar() {
        HBox header = new HBox(20);
        header.getStyleClass().add("header-bar");
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);

        // App name
        Label appName = new Label("LitClub");
        appName.getStyleClass().add("app-name");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User greeting
        String username = service.getCurrentUser() != null ?
                service.getCurrentUser().username() : "User";
        Label greeting = new Label("Hello, " + username);
        greeting.getStyleClass().add("greeting-label");

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("user-button");
        logoutButton.setOnAction(e -> handleLogout());

        header.getChildren().addAll(appName, spacer, greeting, logoutButton);
        return header;
    }

    // ==================== DATA LOADING ====================

    private void loadData() {
        service.loadAllData(
                this::onDataLoaded,
                this::showError
        );
    }

    private void onDataLoaded() {
        contentContainer.getChildren().clear();

        // Build content sections
        contentContainer.getChildren().addAll(
                createMeetingsSection(),
                createCardsSection(),
                createCreateClubSection()
        );
    }

    // ==================== MEETINGS SECTION ====================

    private VBox createMeetingsSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setMaxWidth(900);

        meetingsIsland = new MeetingsIsland(service.getMeetings());

        section.getChildren().add(meetingsIsland);
        return section;
    }

    // ==================== CARDS SECTION ====================

    private VBox createCardsSection() {
        VBox section = new VBox(20);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(1200);

        Label sectionTitle = new Label("My Clubs & Library");
        sectionTitle.getStyleClass().add("section-title");

        // Grid for cards
        clubsGrid = new FlowPane();
        clubsGrid.setHgap(20);
        clubsGrid.setVgap(20);
        clubsGrid.setAlignment(Pos.CENTER);
        clubsGrid.setPrefWrapLength(1200);

        // Add "Me" card first
        clubsGrid.getChildren().add(createMeCard());

        // Add club cards
        for (Club club : service.getClubs()) {
            clubsGrid.getChildren().add(createClubCard(club));
        }

        // Listen for club changes
        service.getClubs().addListener((javafx.collections.ListChangeListener.Change<? extends Club> c) -> {
            refreshClubCards();
        });

        section.getChildren().addAll(sectionTitle, clubsGrid);
        return section;
    }

    private VBox createMeCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        card.setMinHeight(200);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Icon/Avatar
        Label icon = new Label("📚");
        icon.setStyle("-fx-font-size: 48px;");

        // Title
        Label title = new Label("Me");
        title.getStyleClass().add("section-title");
        title.setStyle("-fx-font-size: 22px;");

        // Subtitle
        Label subtitle = new Label("Personal Library & Settings");
        subtitle.getStyleClass().add("section-subtitle");
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setMaxWidth(240);

        // Stats
        VBox stats = new VBox(5);
        stats.setAlignment(Pos.CENTER);

        Label clubCount = new Label("Member of " + service.getClubs().size() + " clubs");
        clubCount.getStyleClass().add("text-muted");
        clubCount.setStyle("-fx-font-size: 12px;");

        stats.getChildren().add(clubCount);

        // Admin badge (if applicable)
        if (service.isAdmin()) {
            Label adminBadge = new Label("Administrator");
            adminBadge.getStyleClass().add("meeting-club-badge");
            adminBadge.setStyle("-fx-font-size: 11px;");
            stats.getChildren().add(adminBadge);
        }

        card.getChildren().addAll(icon, title, subtitle, stats);

        // Click handler
        card.setOnMouseClicked(e -> SceneManager.getInstance().showPersonalPage());

        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle("-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    private VBox createClubCard(Club club) {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(280);
        card.setMinHeight(200);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(20));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Club icon
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 36px;");

        // Club name
        Label name = new Label(club.getClubName());
        name.getStyleClass().add("section-title");
        name.setStyle("-fx-font-size: 18px;");
        name.setWrapText(true);
        name.setMaxWidth(240);

        // Description
        Label description = new Label(
                club.getDescription() != null && !club.getDescription().isEmpty()
                        ? club.getDescription()
                        : "No description"
        );
        description.getStyleClass().add("section-subtitle");
        description.setWrapText(true);
        description.setMaxWidth(240);
        description.setMaxHeight(60);

        // Stats section
        HBox stats = new HBox(10);
        stats.setAlignment(Pos.CENTER_LEFT);

        // Member count (if available)
//        if (club.getMembers() != null) {
//            Label memberCount = new Label(club.getMembers().size() + " members");
//            memberCount.getStyleClass().add("text-muted");
//            memberCount.setStyle("-fx-font-size: 12px;");
//            stats.getChildren().add(memberCount);
//        }

        // Created date
        if (club.getCreatedAt() != null) {
            Label createdDate = new Label("Created " + formatDate(club.getCreatedAt()));
            createdDate.getStyleClass().add("text-muted");
            createdDate.setStyle("-fx-font-size: 11px; -fx-font-style: italic;");
            stats.getChildren().add(createdDate);
        }

        card.getChildren().addAll(icon, name, description, stats);

        // Click handler
        card.setOnMouseClicked(e -> SceneManager.getInstance().showClubPage(club));

        // Hover effects
        card.setOnMouseEntered(e -> card.setStyle("-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return card;
    }

    private void refreshClubCards() {
        clubsGrid.getChildren().clear();

        // Re-add "Me" card
        clubsGrid.getChildren().add(createMeCard());

        // Re-add club cards
        for (Club club : service.getClubs()) {
            clubsGrid.getChildren().add(createClubCard(club));
        }
    }

    // ==================== CREATE CLUB SECTION ====================

    private VBox createCreateClubSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(20, 0, 20, 0));

        // Only show if user can create clubs
        if (!service.canCreateClubs()) {
            return section; // Return empty section
        }

        // Check if user has reached club limit
        if (service.hasReachedClubLimit()) {
            Label limitLabel = new Label("You've reached the maximum number of clubs (" +
                    service.getMaxClubsPerUser() + ")");
            limitLabel.getStyleClass().add("text-muted");
            limitLabel.setStyle("-fx-font-style: italic;");
            section.getChildren().add(limitLabel);
            return section;
        }

        createClubButton = new Button("+ Create New Club");
        createClubButton.getStyleClass().add("button-primary");
        createClubButton.setOnAction(e -> showCreateClubDialog());

        // Show approval notice if needed
        if (service.clubsNeedApproval()) {
            Label approvalNotice = new Label("New clubs require administrator approval");
            approvalNotice.getStyleClass().add("text-muted");
            approvalNotice.setStyle("-fx-font-size: 12px; -fx-font-style: italic;");
            section.getChildren().addAll(createClubButton, approvalNotice);
        } else {
            section.getChildren().add(createClubButton);
        }

        return section;
    }

    private void showCreateClubDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Club");
        dialog.setHeaderText("Enter club details");

        // Dialog buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Club name");
        nameField.setPrefWidth(300);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);

        Label charCountLabel = new Label("0/500");
        charCountLabel.setStyle("-fx-font-size: 11px;");

        descriptionArea.textProperty().addListener((obs, old, newVal) -> {
            charCountLabel.setText(newVal.length() + "/500");
            if (newVal.length() > 500) {
                descriptionArea.setText(old);
            }
        });

        grid.add(new Label("Club Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(charCountLabel, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Focus on name field
        nameField.requestFocus();

        // Handle result
        dialog.showAndWait().ifPresent(response -> {
            if (response == createButtonType) {
                String clubName = nameField.getText().trim();
                String description = descriptionArea.getText().trim();

                if (clubName.isEmpty()) {
                    showError("Club name cannot be empty");
                    return;
                }

                handleCreateClub(clubName, description);
            }
        });
    }

    private void handleCreateClub(String clubName, String description) {
        setLoading(true);

        service.createClub(
                clubName,
                description,
                club -> {
                    setLoading(false);
                    showSuccess("Club '" + club.getClubName() + "' created successfully!");

                    // Navigate to new club after brief delay
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                    pause.setOnFinished(e -> SceneManager.getInstance().showClubPage(club));
                    pause.play();
                },
                error -> {
                    setLoading(false);
                    showError(error);
                }
        );
    }

    // ==================== EVENT HANDLERS ====================

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You'll need to login again to access your clubs.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear session
                AppSession.getInstance().setUserRecord(null);
                AppSession.getInstance().setClubRecord(null);

                // Navigate to login
                SceneManager.getInstance().showLogin();
            }
        });
    }

    // ==================== UI STATE MANAGEMENT ====================

    private void setLoading(boolean loading) {
        if (createClubButton != null) {
            createClubButton.setDisable(loading);
        }

        if (loading) {
            statusLabel.setText("Processing...");
            statusLabel.getStyleClass().removeAll("error-label", "success-label");
            statusLabel.getStyleClass().add("info-label");
            statusLabel.setVisible(true);
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("info-label", "success-label");
        statusLabel.getStyleClass().add("error-label");
        statusLabel.setVisible(true);

        // Auto-hide after 5 seconds
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
        pause.setOnFinished(e -> statusLabel.setVisible(false));
        pause.play();
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label", "info-label");
        statusLabel.getStyleClass().add("success-label");
        statusLabel.setVisible(true);

        // Auto-hide after 3 seconds
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        pause.setOnFinished(e -> statusLabel.setVisible(false));
        pause.play();
    }

    // ==================== UTILITY ====================

    private String formatDate(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        long daysAgo = java.time.temporal.ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate());

        if (daysAgo == 0) {
            return "today";
        } else if (daysAgo == 1) {
            return "yesterday";
        } else if (daysAgo < 7) {
            return daysAgo + " days ago";
        } else if (daysAgo < 30) {
            long weeksAgo = daysAgo / 7;
            return weeksAgo + (weeksAgo == 1 ? " week ago" : " weeks ago");
        } else if (daysAgo < 365) {
            long monthsAgo = daysAgo / 30;
            return monthsAgo + (monthsAgo == 1 ? " month ago" : " months ago");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            return dateTime.format(formatter);
        }
    }
}