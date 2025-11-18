package com.litclub.ui.crossroads;

import com.litclub.SceneManager;
import com.litclub.construct.Club;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.theme.ThemeToggleBar;
import com.litclub.ui.crossroads.components.*;
import com.litclub.ui.crossroads.components.subcomponents.*;
import com.litclub.ui.crossroads.service.CrossRoadsService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * CrossRoads Page - Main dashboard showing user's clubs, meetings, and personal card.
 * Refactored into smaller, focused components.
 */
public class CrossRoadsPage extends BorderPane {

    private final CrossRoadsService service;
    private final ThemeManager themeManager;
    private final StatusBar statusBar;

    private VBox contentContainer;
    private CardsGrid CardsGrid;
    private CreateClubSection createClubSection;
    private ProgressIndicator loadingIndicator;

    public CrossRoadsPage() {
        this.service = new CrossRoadsService();
        this.themeManager = ThemeManager.getInstance();
        this.statusBar = new StatusBar();

        themeManager.registerComponent(this);
        this.getStyleClass().add("root");

        initializeUI();
        loadData();
    }

    private void initializeUI() {
        // Header
        HeaderBar headerBar = new HeaderBar(service, this::handleLogout);
        setTop(headerBar);

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

        // Status bar at bottom
        setBottom(statusBar);
    }

    private void loadData() {
        service.loadAllData(
                this::onDataLoaded,
                statusBar::showError
        );
    }

    private void onDataLoaded() {
        contentContainer.getChildren().clear();

        // Meetings section
        MeetingsSection meetingsSection = new MeetingsSection(service.getMeetings());

        // Club cards grid
        CardsGrid = new CardsGrid(
                service,
                this::handleNavigateToPersonal,
                this::handleNavigateToClub,
                this::redeemInvite
        );

        // Create club section
        createClubSection = new CreateClubSection(
                service,
                this::showCreateClubDialog
        );

        contentContainer.getChildren().addAll(
                meetingsSection,
                CardsGrid,
                createClubSection
        );
    }

    private void showCreateClubDialog() {
        CreateClubDialog dialog = new CreateClubDialog();
        dialog.showAndWait().ifPresent(clubData -> {
            handleCreateClub(clubData.name(), clubData.description());
        });
    }

    private void handleCreateClub(String clubName, String description) {
        statusBar.showLoading();
        createClubSection.setDisable(true);

        service.createClub(
                clubName,
                description,
                club -> {
                    statusBar.hideLoading();
                    createClubSection.setDisable(false);
                    statusBar.showSuccess("Club '" + club.getClubName() + "' created successfully!");

                    // Navigate after delay
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                    pause.setOnFinished(e -> SceneManager.getInstance().showMainPage(false));
                    pause.play();
                },
                error -> {
                    statusBar.hideLoading();
                    createClubSection.setDisable(false);
                    statusBar.showError(error);
                }
        );
    }

    private void handleLogout() {
        LogoutConfirmation.show(() -> {
            AppSession.getInstance().clearClubContext();
            AppSession.getInstance().setUserRecord(null);
            SceneManager.getInstance().showLogin();
        });
    }

    private void handleNavigateToPersonal() {
        service.preparePersonalContext();
        SceneManager.getInstance().showMainPage(true);
    }

    private void handleNavigateToClub(Club club) {
        statusBar.showLoading();
        CardsGrid.setDisable(true);

        service.prepareClubContext(
                club,
                () -> {
                    statusBar.hideLoading();
                    CardsGrid.setDisable(false);
                    SceneManager.getInstance().showMainPage(false);
                },
                error -> {
                    statusBar.hideLoading();
                    CardsGrid.setDisable(false);
                    statusBar.showError("Failed to enter club: " + error);
                }
        );
    }

    private void redeemInvite() {
        RedeemInviteDialog inviteDialog = new RedeemInviteDialog(service);
        inviteDialog.showAndWait().ifPresent(code -> System.out.println("Redeemed invitation: " + code));
    }
}

// ==================== SUPPORTING COMPONENTS ====================

/**
 * Header bar with app name, greeting, and logout button
 */
class HeaderBar extends HBox {
    public HeaderBar(CrossRoadsService service, Runnable onLogout) {
        super(20);
        getStyleClass().add("header-bar");
        setPadding(new Insets(15, 30, 15, 30));
        setAlignment(Pos.CENTER_LEFT);

        Label appName = new Label("LitClub");
        appName.getStyleClass().add("app-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String username = service.getCurrentUser() != null ?
                service.getCurrentUser().username() : "User";
        Label greeting = new Label("Hello, " + username);
        greeting.getStyleClass().add("greeting-label");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("user-button");
        logoutButton.setOnAction(e -> onLogout.run());

        ThemeToggleBar toggleBar = new ThemeToggleBar();

        getChildren().addAll(appName, toggleBar, spacer, greeting, logoutButton);
    }
}

/**
 * Status bar for showing messages at the bottom
 */
class StatusBar extends Label {
    public StatusBar() {
        setVisible(false);
        getStyleClass().add("status-label");
        setPadding(new Insets(10));
    }

    public void showLoading() {
        setText("Processing...");
        getStyleClass().removeAll("error-label", "success-label");
        getStyleClass().add("info-label");
        setVisible(true);
    }

    public void hideLoading() {
        setVisible(false);
    }

    public void showError(String message) {
        setText(message);
        getStyleClass().removeAll("info-label", "success-label");
        getStyleClass().add("error-label");
        setVisible(true);
        autoHide(5);
    }

    public void showSuccess(String message) {
        setText(message);
        getStyleClass().removeAll("error-label", "info-label");
        getStyleClass().add("success-label");
        setVisible(true);
        autoHide(3);
    }

    private void autoHide(int seconds) {
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(seconds));
        pause.setOnFinished(e -> setVisible(false));
        pause.play();
    }
}

/**
 * Logout confirmation dialog
 */
class LogoutConfirmation {
    public static void show(Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("You'll need to login again to access your clubs.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }
}