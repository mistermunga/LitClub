package com.litclub.ui.main.shared.view.subcomponent.discussions;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Note;
import com.litclub.ui.main.shared.view.service.DiscussionService;
import com.litclub.ui.main.shared.view.subcomponent.common.AbstractFocusView;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Focused view showing a discussion prompt with all response notes.
 * Displays the prompt at the top and a grid of notes below.
 */
public class PromptFocus extends AbstractFocusView<DiscussionPrompt> {

    private final DiscussionService discussionService;
    private final Consumer<Note> onNoteClick;
    private VBox notesSection;
    private FlowPane notesPane;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy 'at' hh:mm a");

    public PromptFocus(DiscussionService discussionService, Runnable onBack, Consumer<Note> onNoteClick) {
        super("discussion-focus", onBack);
        this.discussionService = discussionService;
        this.onNoteClick = onNoteClick;
    }

    /**
     * Load and display a discussion prompt with its notes.
     */
    public void loadPrompt(DiscussionPrompt prompt) {
        load(prompt);
    }

    @Override
    protected VBox createHeaderDetails() {
        VBox details = new VBox(12);
        details.getStyleClass().add("prompt-info");

        // Prompt text
        Label promptLabel = new Label(currentEntity.getPrompt());
        promptLabel.getStyleClass().add("section-title");
        promptLabel.setStyle("-fx-font-size: 20px;");
        promptLabel.setWrapText(true);
        promptLabel.setMaxWidth(Double.MAX_VALUE);

        // Posted date
        String postedAt = currentEntity.getPostedAt() != null
                ? currentEntity.getPostedAt().format(DATE_FORMATTER)
                : "Unknown date";
        Label dateLabel = new Label("📝 Posted " + postedAt);
        dateLabel.getStyleClass().add("text-muted");
        dateLabel.setStyle("-fx-font-size: 13px;");

        details.getChildren().addAll(promptLabel, dateLabel);
        return details;
    }

    @Override
    protected void buildContent() {
        loadNotesSection();
    }

    private void loadNotesSection() {
        clearContent();

        // Create notes section with loading indicator
        notesSection = createCardSection();

        Label notesHeader = new Label("Discussion Responses");
        notesHeader.getStyleClass().add("section-title");

        // Add button for creating a response
        Button addResponseButton = new Button("+ Add Response");
        addResponseButton.getStyleClass().add("button-primary");
        addResponseButton.setOnAction(e -> handleAddResponse());

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        VBox loadingBox = new VBox(loadingIndicator);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(20));

        notesSection.getChildren().addAll(notesHeader, addResponseButton, loadingBox);
        addContentSection(notesSection);

        // Fetch notes asynchronously
        discussionService.loadPromptNotes(
                currentEntity.getPromptID(),
                this::displayNotes,
                this::displayNotesError
        );
    }

    private void displayNotes() {
        // Clear loading indicator
        notesSection.getChildren().clear();

        Label notesHeader = new Label("Discussion Responses");
        notesHeader.getStyleClass().add("section-title");

        Button addResponseButton = new Button("+ Add Response");
        addResponseButton.getStyleClass().add("button-primary");
        addResponseButton.setOnAction(e -> handleAddResponse());

        notesSection.getChildren().addAll(notesHeader, addResponseButton);

        // Create notes grid
        notesPane = new FlowPane();
        notesPane.getStyleClass().add("notes-container");
        notesPane.setHgap(15);
        notesPane.setVgap(15);
        notesPane.setPadding(new Insets(15, 0, 0, 0));

        ObservableList<Note> notes = discussionService.getPromptNotes();

        if (notes.isEmpty()) {
            Label emptyLabel = new Label("No responses yet. Be the first to respond!");
            emptyLabel.getStyleClass().add("empty-notes-label");
            notesPane.getChildren().add(emptyLabel);
        } else {
            populateNotes(notes);
        }

        notesSection.getChildren().add(notesPane);

        // Setup data listener for live updates
        setupNotesListener();
    }

    private void displayNotesError(String errorMessage) {
        // Clear loading indicator
        notesSection.getChildren().clear();

        Label notesHeader = new Label("Discussion Responses");
        notesHeader.getStyleClass().add("section-title");

        Label errorLabel = new Label("Failed to load responses: " + errorMessage);
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        Button retryButton = new Button("Retry");
        retryButton.getStyleClass().add("secondary-button");
        retryButton.setOnAction(e -> loadNotesSection());

        notesSection.getChildren().addAll(notesHeader, errorLabel, retryButton);
    }

    private void populateNotes(ObservableList<Note> notes) {
        notesPane.getChildren().clear();

        for (Note note : notes) {
            VBox noteCard = createNoteCard(note);
            notesPane.getChildren().add(noteCard);
        }
    }

    private VBox createNoteCard(Note note) {
        VBox card = new VBox();
        card.getStyleClass().add("note-card");
        card.setPadding(new Insets(16));
        card.setSpacing(10);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Note content (truncate if too long)
        String content = note.getContent();
        if (content != null && content.length() > 150) {
            content = content.substring(0, 147) + "...";
        }
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("note-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(280);

        // Book reference
        String bookTitle = note.getBook() != null && note.getBook().getTitle() != null
                ? note.getBook().getTitle()
                : "Unknown Book";
        Label bookLabel = new Label("📚 " + bookTitle);
        bookLabel.getStyleClass().add("note-book-reference");
        bookLabel.setWrapText(true);
        bookLabel.setMaxWidth(280);

        // Date
        String dateStr = note.getCreatedAt() != null
                ? note.getCreatedAt().format(DATE_FORMATTER)
                : "Unknown date";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("note-date");

        // Author
        String author = note.getUser() != null && note.getUser().getUsername() != null
                ? note.getUser().getUsername()
                : "Unknown author";
        Label authorLabel = new Label("✍️ " + author);
        authorLabel.getStyleClass().add("note-shared-indicator");

        card.getChildren().addAll(contentLabel, bookLabel, dateLabel, authorLabel);

        // Click handler - navigate to note focus
        card.setOnMouseClicked(e -> onNoteClick.accept(note));

        return card;
    }

    private void setupNotesListener() {
        ObservableList<Note> notes = discussionService.getPromptNotes();

        notes.addListener((ListChangeListener.Change<? extends Note> c) -> {
            System.out.println("Prompt notes list changed!");
            if (notesPane != null) {
                populateNotes(notes);
            }
        });
    }

    // ==================== EVENT HANDLERS ====================

    private void handleAddResponse() {
        System.out.println("Add response to prompt: " + currentEntity.getPromptID());
        // TODO: Open dialog to add note for this prompt
    }

    /**
     * Get the currently displayed prompt.
     */
    public DiscussionPrompt getCurrentPrompt() {
        return currentEntity;
    }
}