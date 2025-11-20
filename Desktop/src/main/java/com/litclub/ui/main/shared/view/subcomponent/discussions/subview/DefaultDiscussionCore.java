package com.litclub.ui.main.shared.view.subcomponent.discussions.subview;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.DiscussionService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.function.Consumer;

/**
 * Default discussion core view showing all prompts in a list.
 * Context-aware: shows discussion prompts for the current club.
 */
public class DefaultDiscussionCore extends ScrollPane {

    private final DiscussionService discussionService;
    private final Consumer<DiscussionPrompt> onPromptClick;
    private final VBox container;

    private ObservableList<DiscussionPrompt> prompts;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy  •  h:mm a");

    public DefaultDiscussionCore(Consumer<DiscussionPrompt> onPromptClick) {
        this.discussionService = new DiscussionService();
        this.onPromptClick = onPromptClick;

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("discussion-core", "scroll-pane");

        container = new VBox(20);
        container.setPadding(new Insets(30, 40, 30, 40));
        container.getStyleClass().add("discussion-container");

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
        loadPrompts();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        this.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    // ==================== DATA LOADING ====================

    public void loadPrompts() {
        discussionService.loadPrompts(
                AppSession.getInstance().getCurrentClub().getClubID(),
                this::onPromptsLoaded,
                this::showError
        );
    }

    private void onPromptsLoaded() {
        Platform.runLater(() -> {
            // Hide loading indicator
            container.getChildren().clear();

            // Build prompts view
            buildPromptsView();

            System.out.println("Prompts loaded successfully!");
        });
    }

    private void buildPromptsView() {
        // Add header
        Label headerLabel = new Label("Discussion Prompts");
        headerLabel.getStyleClass().add("discussion-header");
        container.getChildren().add(headerLabel);

        // Get and sort prompts
        prompts = discussionService.getDiscussionPrompts();
        prompts.sort(Comparator.comparing(DiscussionPrompt::getPostedAt).reversed());

        // Populate prompts
        refreshPrompts();
    }

    // ==================== PROMPT REFRESH ====================

    private void refreshPrompts() {
        // Remove prompt cards or empty-state messages, but keep the header
        container.getChildren().removeIf(node ->
                node.getStyleClass().contains("prompt-card") ||
                        node.getStyleClass().contains("empty-state")
        );

        if (prompts == null || prompts.isEmpty()) {
            Label emptyState = new Label("No discussion prompts posted yet");
            emptyState.getStyleClass().add("empty-state");
            container.getChildren().add(emptyState);
            return;
        }

        for (DiscussionPrompt prompt : prompts) {
            VBox card = createPromptCard(prompt);
            container.getChildren().add(card);
        }
    }

    // ==================== UI BUILDERS ====================

    private VBox createPromptCard(DiscussionPrompt prompt) {
        VBox promptCard = new VBox(12);
        promptCard.setPadding(new Insets(20, 24, 20, 24));
        promptCard.getStyleClass().add("prompt-card");
        promptCard.setCursor(javafx.scene.Cursor.HAND);

        // Prompt text
        Label promptLabel = new Label(prompt.getPrompt());
        promptLabel.getStyleClass().add("prompt-title");
        promptLabel.setWrapText(true);

        // Posted timestamp
        String postedAt = prompt.getPostedAt().format(TIME_FORMATTER);
        Label postedAtLabel = new Label("📝 Posted " + postedAt);
        postedAtLabel.getStyleClass().add("prompt-timestamp");
        postedAtLabel.setWrapText(true);

        promptCard.getChildren().addAll(promptLabel, postedAtLabel);

        // Click handler - open prompt focus
        promptCard.setOnMouseClicked(e -> onPromptClick.accept(prompt));

        return promptCard;
    }

    // ==================== LOADING & ERROR STATES ====================

    private void showLoading() {
        container.getChildren().clear();

        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(100));

        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(50, 50);

        Label loadingLabel = new Label("Loading discussion prompts...");
        loadingLabel.getStyleClass().add("section-subtitle");

        loadingBox.getChildren().addAll(loadingIndicator, loadingLabel);
        container.getChildren().add(loadingBox);
    }

    private void showError(String errorMessage) {
        Platform.runLater(() -> {
            container.getChildren().clear();

            VBox errorBox = new VBox(20);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(100));

            Label errorIcon = new Label("⚠️");
            errorIcon.setStyle("-fx-font-size: 48px;");

            Label errorLabel = new Label("Failed to load discussion prompts");
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

    // ==================== PUBLIC API ====================

    /**
     * Refresh prompts display.
     */
    public void refresh() {
        refreshPrompts();
    }

    /**
     * Get the discussion service instance.
     */
    public DiscussionService getDiscussionService() {
        return discussionService;
    }
}
