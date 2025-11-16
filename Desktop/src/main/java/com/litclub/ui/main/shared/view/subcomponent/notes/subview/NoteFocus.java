package com.litclub.ui.main.shared.view.subcomponent.notes.subview;

import com.litclub.construct.Note;
import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Focused view showing a single note with its details and replies.
 * Currently displays note details at the top - replies to be implemented.
 */
public class NoteFocus extends ScrollPane {

    private final boolean isPersonal;
    private final Runnable onBack;
    private final VBox container;
    private final HBox noteHeader;

    private Note currentNote;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy 'at' hh:mm a");

    public NoteFocus(boolean isPersonal, Runnable onBack) {
        this.isPersonal = isPersonal;
        this.onBack = onBack;

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("notes-core", "scroll-pane");

        // Main container
        container = new VBox(30);
        container.setPadding(new Insets(20));
        container.getStyleClass().add("container");

        // Note header (will be populated when note is loaded)
        noteHeader = new HBox(20);
        noteHeader.getStyleClass().add("card");
        noteHeader.setPadding(new Insets(24));
        noteHeader.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().add(noteHeader);

        // Scroll pane setup
        this.setContent(container);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        setupSmoothScrolling();
    }

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        container.setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }

    /**
     * Load and display a note.
     */
    public void loadNote(Note note) {
        this.currentNote = note;
        buildNoteHeader();

        // TODO: Load and display replies here
        // For now, just show a placeholder
        addRepliesPlaceholder();
    }

    private void buildNoteHeader() {
        noteHeader.getChildren().clear();

        // Left side: Back button
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("secondary-button");
        backButton.setOnAction(e -> onBack.run());

        // Center: Note details
        VBox noteDetails = createNoteDetails();
        HBox.setHgrow(noteDetails, Priority.ALWAYS);

        noteHeader.getChildren().addAll(backButton, noteDetails);
    }

    private VBox createNoteDetails() {
        VBox details = new VBox(12);
        details.getStyleClass().add("note-info");

        // Book title
        String bookTitle = currentNote.getBook() != null && currentNote.getBook().getTitle() != null
                ? currentNote.getBook().getTitle()
                : "Unknown Book";
        Label bookLabel = new Label("📚 " + bookTitle);
        bookLabel.getStyleClass().add("section-title");
        bookLabel.setStyle("-fx-font-size: 18px;");

        // Note content
        Label contentLabel = new Label(currentNote.getContent());
        contentLabel.getStyleClass().add("note-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-font-size: 15px;");

        // Metadata row
        HBox metadataRow = createMetadataRow();

        details.getChildren().addAll(bookLabel, contentLabel, metadataRow);
        return details;
    }

    private HBox createMetadataRow() {
        HBox metadata = new HBox(20);
        metadata.setAlignment(Pos.CENTER_LEFT);

        // Author (for club notes) or privacy indicator (for personal notes)
        if (isPersonal) {
            Label privateLabel = new Label("🔒 Private Note");
            privateLabel.getStyleClass().add("note-private-indicator");
            metadata.getChildren().add(privateLabel);
        } else {
            String author = currentNote.getUser() != null && currentNote.getUser().getUsername() != null
                    ? currentNote.getUser().getUsername()
                    : "Unknown author";
            Label authorLabel = new Label("✍️ " + author);
            authorLabel.getStyleClass().add("note-shared-indicator");
            metadata.getChildren().add(authorLabel);
        }

        // Date
        String dateStr = currentNote.getCreatedAt() != null
                ? currentNote.getCreatedAt().format(DATE_FORMATTER)
                : "Unknown date";
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.getStyleClass().add("note-date");
        metadata.getChildren().add(dateLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        metadata.getChildren().add(spacer);

        // Edit/Delete buttons (only if user owns the note)
        if (isCurrentUserOwner()) {
            Button editButton = new Button("Edit");
            editButton.getStyleClass().add("secondary-button");
            editButton.setOnAction(e -> handleEditNote());

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("secondary-button");
            deleteButton.setOnAction(e -> handleDeleteNote());

            metadata.getChildren().addAll(editButton, deleteButton);
        }

        return metadata;
    }

    private void addRepliesPlaceholder() {
        // Clear any existing content below header
        if (container.getChildren().size() > 1) {
            container.getChildren().remove(1, container.getChildren().size());
        }

        // Add placeholder for replies section
        VBox repliesSection = new VBox(15);
        repliesSection.getStyleClass().add("card");
        repliesSection.setPadding(new Insets(24));

        Label repliesHeader = new Label(isPersonal ? "Notes" : "Replies");
        repliesHeader.getStyleClass().add("section-title");

        Label placeholder = new Label(isPersonal
                ? "Personal notes don't have replies"
                : "Replies will be displayed here");
        placeholder.getStyleClass().add("text-muted");
        placeholder.setStyle("-fx-font-style: italic;");

        repliesSection.getChildren().addAll(repliesHeader, placeholder);
        container.getChildren().add(repliesSection);
    }

    // ==================== HELPER METHODS ====================

    private boolean isCurrentUserOwner() {
        if (currentNote == null || currentNote.getUser() == null) {
            return false;
        }

        Long currentUserId = com.litclub.session.AppSession.getInstance()
                .getUserRecord().userID();

        return currentNote.getUser().getUserID().equals(currentUserId);
    }

    private void handleEditNote() {
        System.out.println("Edit note: " + currentNote.getNoteID());
        // TODO: Open edit dialog
    }

    private void handleDeleteNote() {
        System.out.println("Delete note: " + currentNote.getNoteID());
        // TODO: Show confirmation dialog and delete
    }

    /**
     * Get the currently displayed note.
     */
    public Note getCurrentNote() {
        return currentNote;
    }
}