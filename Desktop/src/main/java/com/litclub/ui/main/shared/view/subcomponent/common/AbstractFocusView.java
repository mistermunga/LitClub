package com.litclub.ui.main.shared.view.subcomponent.common;

import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Abstract base class for focus views (BookFocus, NoteFocus, PromptFocus).
 * Provides common structure: header with back button, scrollable content area,
 * and smooth scrolling behavior.
 *
 * @param <T> the type of entity being focused on (Book, Note, DiscussionPrompt, etc.)
 */
public abstract class AbstractFocusView<T> extends ScrollPane {

    protected final Runnable onBack;
    protected final VBox container;
    protected final HBox header;

    protected T currentEntity;

    private static final double SMOOTH_SCROLL_SPEED = 0.005;

    /**
     * Creates a new focus view.
     *
     * @param styleClass additional style class for the view
     * @param onBack callback when back button is clicked
     */
    protected AbstractFocusView(String styleClass, Runnable onBack) {
        this.onBack = onBack;

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll(styleClass, "scroll-pane");

        // Main container
        container = new VBox(30);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("container");

        // Header (will be populated by subclasses)
        header = new HBox(20);
        header.getStyleClass().add("card");
        header.setPadding(new Insets(24));
        header.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().add(header);

        // Scroll pane setup
        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        setupSmoothScrolling();
    }

    /**
     * Sets up smooth scrolling behavior for the container.
     */
    private void setupSmoothScrolling() {
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SMOOTH_SCROLL_SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    /**
     * Loads and displays an entity.
     * Subclasses must implement this to populate the view.
     *
     * @param entity the entity to display
     */
    public void load(T entity) {
        this.currentEntity = entity;
        buildHeader();
        buildContent();
    }

    /**
     * Builds the header section with back button and entity details.
     * Subclasses can override to customize header layout.
     */
    protected void buildHeader() {
        header.getChildren().clear();

        // Left side: Back button
        Button backButton = createBackButton();

        // Center: Entity details
        VBox details = createHeaderDetails();
        HBox.setHgrow(details, Priority.ALWAYS);

        header.getChildren().addAll(backButton, details);
    }

    /**
     * Creates the back button.
     * Subclasses can override to customize button appearance.
     */
    protected Button createBackButton() {
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> onBack.run());
        return backButton;
    }

    /**
     * Creates the header details section.
     * Subclasses must implement this to show entity-specific information.
     *
     * @return VBox containing header details
     */
    protected abstract VBox createHeaderDetails();

    /**
     * Builds the main content section below the header.
     * Subclasses must implement this to show entity-specific content
     * (reviews, replies, notes, etc.).
     */
    protected abstract void buildContent();

    /**
     * Clears all content below the header.
     * Useful before rebuilding or refreshing content.
     */
    protected void clearContent() {
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }
    }

    /**
     * Adds a section to the content area.
     *
     * @param section the VBox section to add
     */
    protected void addContentSection(VBox section) {
        container.getChildren().add(section);
    }

    /**
     * Creates a standard card-styled section.
     *
     * @return empty VBox with card styling
     */
    protected VBox createCardSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(24));
        return section;
    }

    /**
     * Gets the currently displayed entity.
     *
     * @return current entity or null if none loaded
     */
    public T getCurrentEntity() {
        return currentEntity;
    }

    /**
     * Checks if an entity is currently loaded.
     *
     * @return true if entity is loaded
     */
    public boolean hasEntity() {
        return currentEntity != null;
    }
}