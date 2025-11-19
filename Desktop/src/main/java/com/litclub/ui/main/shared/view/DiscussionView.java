package com.litclub.ui.main.shared.view;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.view.service.DiscussionService;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class DiscussionView extends ScrollPane {

    private final DiscussionService discussionService;
    private ObservableList<DiscussionPrompt> prompts;

    private final VBox container;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy  •  h:mm a");

    public DiscussionView() {
        discussionService = new DiscussionService();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("discussion-view", "scroll-pane");
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.container = new VBox(20);
        this.container.setPadding(new Insets(30, 40, 30, 40));
        this.container.getStyleClass().add("discussion-container");
        VBox.setVgrow(this.container, Priority.ALWAYS);

        this.setVvalue(0);
        this.setPannable(false);

        setupSmoothScrolling();

        addHeader();
        showPrompts();

        this.setContent(container);
    }

    private void addHeader() {
        Label headerLabel = new Label("Discussion Prompts");
        headerLabel.getStyleClass().add("discussion-header");
        container.getChildren().add(headerLabel);
    }

    public void showPrompts() {
        prompts = discussionService.getDiscussionPrompts();
        prompts.sort(Comparator.comparing(DiscussionPrompt::getPostedAt).reversed());

        // Clear only prompt cards, keep the header
        container.getChildren().removeIf(node ->
                node.getStyleClass().contains("prompt-card")
        );

        if (prompts.isEmpty()) {
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

    private VBox createPromptCard(DiscussionPrompt prompt) {
        VBox promptCard = new VBox(12);
        promptCard.setPadding(new Insets(20, 24, 20, 24));
        promptCard.getStyleClass().add("prompt-card");

        // Prompt text
        Label promptLabel = new Label(prompt.getPrompt());
        promptLabel.getStyleClass().add("prompt-title");
        promptLabel.setWrapText(true);

        // Posted at timestamp
        String postedAt = prompt.getPostedAt().format(TIME_FORMATTER);
        Label postedAtLabel = new Label("📝 Posted " + postedAt);
        postedAtLabel.getStyleClass().add("prompt-timestamp");
        postedAtLabel.setWrapText(true);

        promptCard.getChildren().addAll(promptLabel, postedAtLabel);

        return promptCard;
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        this.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }
}