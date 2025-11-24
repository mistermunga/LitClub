package com.litclub.ui.main.shared.view.subcomponent.notes.subview;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Note;
import com.litclub.construct.Reply;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.view.service.ReplyService;
import com.litclub.ui.main.shared.view.subcomponent.common.AbstractFocusView;
import com.litclub.ui.main.shared.view.subcomponent.replies.dialog.AddReplyDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/**
 * Focused view showing a single note with its details and replies.
 */
public class NoteFocus extends AbstractFocusView<Note> {

    private final boolean isPersonal;
    private final ReplyService replyService = new  ReplyService();
    private final AppSession session = AppSession.getInstance();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM, yyyy 'at' hh:mm a");
    private FlowPane replyPane;
    private DiscussionPrompt prompt;
    private ObservableList<Reply> replies = FXCollections.observableArrayList();

    public NoteFocus(boolean isPersonal, Runnable onBack) {
        super("notes-core", onBack);
        this.isPersonal = isPersonal;
    }

    public NoteFocus(Runnable onBack, DiscussionPrompt prompt) {
        super("notes-core", onBack);
        this.isPersonal = false;
        this.prompt = prompt;
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
        if (isPersonal) { return; }
        if (prompt != null) {
            replyService.loadDiscussionReplies(
                    session.getCurrentClub().getClubID(),
                    prompt.getPromptID(),
                    currentEntity.getNoteID(),
                    // success
                    () -> System.out.println("loaded replies for ptompt note"),
                    // error
                    null
            );
        } else {
            replyService.loadIndependentReplies(
                    currentEntity.getBook().getBookID(),
                    currentEntity.getNoteID(),
                    () -> System.out.println("Loaded replies"),
                    error -> System.out.println("Failed to fetch Replies " + error)
            );
        }

        this.replies = replyService.getReplies();

        if (replies.isEmpty()) {
            addRepliesPlaceholder();
            return;
        }

        createRepliesSection();
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
            Label authorLabel = new Label("✍" + author);
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

        if (!isPersonal) {
            Button addReply = new Button("Add Reply");
            addReply.getStyleClass().add("button-primary");
            addReply.setOnAction(e -> handleAddReply());
            metadata.getChildren().add(addReply);
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

    public void handleAddReply() {
        AddReplyDialog dialog;
        if (prompt != null) {
            dialog = AddReplyDialog.forDiscussionPrompt(
                    prompt.getPromptID(),
                    currentEntity.getNoteID()
            );
        } else {
            dialog = AddReplyDialog.forIndependentNote(
                    currentEntity.getBook().getBookID(),
                    currentEntity.getNoteID()
            );
        }
        dialog.showAndWait();
        EventBus.getInstance().emit(EventBus.EventType.REPLIES_ADDED);
    }

    private void createRepliesSection() {
        clearContent();

        // Create replies section container
        VBox repliesSection = createCardSection();

        // Section header
        Label repliesHeader = new Label("Replies (" + replies.size() + ")");
        repliesHeader.getStyleClass().add("section-title");

        // Replies container with proper spacing
        VBox repliesContainer = new VBox(15);
        repliesContainer.getStyleClass().add("replies-container");
        repliesContainer.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        // Populate replies
        for (Reply reply : replies) {
            VBox replyCard = createReplyCard(reply);
            repliesContainer.getChildren().add(replyCard);
        }

        repliesSection.getChildren().addAll(repliesHeader, repliesContainer);
        addContentSection(repliesSection);
    }

    private VBox createReplyCard(Reply reply) {
        VBox card = new VBox(12);
        card.getStyleClass().add("reply-card");
        card.setPadding(new javafx.geometry.Insets(16));

        // Header with author and date
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Author name
        String authorName = reply.getUser() != null && reply.getUser().getUsername() != null
                ? reply.getUser().getUsername()
                : "Unknown User";
        Label authorLabel = new Label("✍️ " + authorName);
        authorLabel.getStyleClass().add("reply-author");
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Date
        String dateStr = reply.getCreatedAt() != null
                ? reply.getCreatedAt().format(DATE_FORMATTER)
                : "Unknown date";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("reply-date");
        dateLabel.setStyle("-fx-text-fill: -fx-text-muted;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(authorLabel, spacer, dateLabel);

        // Reply content
        Label contentLabel = new Label(reply.getContent());
        contentLabel.getStyleClass().add("reply-content");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setStyle("-fx-font-size: 14px; -fx-line-spacing: 0.3em;");

        card.getChildren().addAll(header, contentLabel);

        // Add edit/delete buttons if current user is the owner
        if (isCurrentUserReplyOwner(reply)) {
            HBox actionButtons = new HBox(10);
            actionButtons.setAlignment(Pos.CENTER_RIGHT);
            actionButtons.setPadding(new javafx.geometry.Insets(8, 0, 0, 0));

            Button editButton = new Button("Edit");
            editButton.getStyleClass().add("secondary-button");
            editButton.setStyle("-fx-font-size: 12px; -fx-padding: 4 12;");
            editButton.setOnAction(e -> handleEditReply(reply));

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("secondary-button");
            deleteButton.setStyle("-fx-font-size: 12px; -fx-padding: 4 12;");
            deleteButton.setOnAction(e -> handleDeleteReply(reply));

            actionButtons.getChildren().addAll(editButton, deleteButton);
            card.getChildren().add(actionButtons);
        }

        return card;
    }

    private boolean isCurrentUserReplyOwner(Reply reply) {
        if (reply == null || reply.getUser() == null) {
            return false;
        }

        Long currentUserId = session.getUserRecord().userID();
        return reply.getUser().getUserID().equals(currentUserId);
    }

    private void handleEditReply(Reply reply) {
        System.out.println("Edit reply: " + reply.getNoteID());
        // TODO: Implement edit dialog
    }

    private void handleDeleteReply(Reply reply) {
        System.out.println("Delete reply: " + reply.getNoteID());
        // TODO: Implement delete confirmation
    }

}