package com.litclub.ui.main.shared.view.subcomponent.notes.subview;

import com.litclub.construct.Note;
import com.litclub.ui.main.shared.view.subcomponent.common.AbstractFocusView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Focused view showing a single note with its details and replies.
 * Currently displays note details at the top - replies to be implemented.
 */
public class NoteFocus extends AbstractFocusView<Note> {

    private final boolean isPersonal;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy 'at' hh:mm a");

    public NoteFocus(boolean isPersonal, Runnable onBack) {
        super("notes-core", onBack);
        this.isPersonal = isPersonal;
    }

    /**
     * Load and display a note.
     */
    public void loadNote(Note note) {
        load(note);
    }

    @Override
    protected VBox createHeaderDetails() {
        VBox details = new VBox(12);
        details.getStyleClass().add("note-info");

        // Book title
        String bookTitle = currentEntity.getBook() != null && currentEntity.getBook().getTitle() != null
                ? currentEntity.getBook().getTitle()
                : "Unknown Book";
        Label bookLabel = new Label("📚 " + bookTitle);
        bookLabel.getStyleClass().add("section-title");
        bookLabel.setStyle("-fx-font-size: 18px;");

        // Note content
        Label contentLabel = new Label(currentEntity.getContent());
        contentLabel.getStyleClass().add("note-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-font-size: 15px;");

        // Metadata row
        HBox metadataRow = createMetadataRow();

        details.getChildren().addAll(bookLabel, contentLabel, metadataRow);
        return details;
    }

    @Override
    protected void buildContent() {
        // TODO: Load and display replies here
        // For now, just show a placeholder
        addRepliesPlaceholder();
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
            String author = currentEntity.getUser() != null && currentEntity.getUser().getUsername() != null
                    ? currentEntity.getUser().getUsername()
                    : "Unknown author";
            Label authorLabel = new Label("✍️ " + author);
            authorLabel.getStyleClass().add("note-shared-indicator");
            metadata.getChildren().add(authorLabel);
        }

        // Date
        String dateStr = currentEntity.getCreatedAt() != null
                ? currentEntity.getCreatedAt().format(DATE_FORMATTER)
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
        clearContent();

        // Add placeholder for replies section
        VBox repliesSection = createCardSection();

        Label repliesHeader = new Label(isPersonal ? "Notes" : "Replies");
        repliesHeader.getStyleClass().add("section-title");

        Label placeholder = new Label(isPersonal
                ? "Personal notes don't have replies"
                : "Replies will be displayed here");
        placeholder.getStyleClass().add("text-muted");
        placeholder.setStyle("-fx-font-style: italic;");

        repliesSection.getChildren().addAll(repliesHeader, placeholder);
        addContentSection(repliesSection);
    }

    // ==================== HELPER METHODS ====================

    private boolean isCurrentUserOwner() {
        if (currentEntity == null || currentEntity.getUser() == null) {
            return false;
        }

        Long currentUserId = com.litclub.session.AppSession.getInstance()
                .getUserRecord().userID();

        return currentEntity.getUser().getUserID().equals(currentUserId);
    }

    private void handleEditNote() {
        System.out.println("Edit note: " + currentEntity.getNoteID());
        // TODO: Open edit dialog
    }

    private void handleDeleteNote() {
        System.out.println("Delete note: " + currentEntity.getNoteID());
        // TODO: Show confirmation dialog and delete
    }

    /**
     * Get the currently displayed note.
     */
    public Note getCurrentNote() {
        return currentEntity;
    }
}