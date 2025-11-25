package com.litclub.ui.main.shared.view.subcomponent.replies.dialog;

import com.litclub.construct.Reply;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.ReplyService;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

/**
 * Polymorphic dialog for adding replies to notes.
 * Supports both discussion prompt replies and independent club note replies.
 */
public class AddReplyDialog extends BaseAsyncDialog<Reply> {

    private final ReplyService replyService;
    private final ReplyContext context;

    private TextArea contentArea;

    // ==================== CONTEXT CLASSES ====================

    /**
     * Context information for creating a reply.
     * Sealed interface with two implementations.
     */
    private sealed interface ReplyContext permits DiscussionContext, IndependentContext {}

    /**
     * Context for discussion prompt replies.
     */
    private record DiscussionContext(
            Long clubID,
            Long promptID,
            Long noteID
    ) implements ReplyContext {}

    /**
     * Context for independent club note replies.
     */
    private record IndependentContext(
            Long bookID,
            Long noteID
    ) implements ReplyContext {}

    // ==================== PRIVATE CONSTRUCTOR ====================

    /**
     * Private constructor - use static factory methods instead.
     */
    private AddReplyDialog(ReplyContext context) {
        super("Add Reply", "Add Reply");
        this.replyService = new ReplyService();
        this.context = context;
        setHeaderText("Reply to this note");
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create dialog for discussion prompt replies.
     *
     * @param promptID the discussion prompt ID
     * @param noteID the note ID being replied to
     * @return configured dialog instance
     */
    public static AddReplyDialog forDiscussionPrompt(Long promptID, Long noteID) {
        Long clubID = AppSession.getInstance().getCurrentClub().getClubID();
        AddReplyDialog dialog = new AddReplyDialog(new DiscussionContext(clubID, promptID, noteID));
        dialog.initializeUI();
        return dialog;
    }

    /**
     * Create dialog for independent club note replies.
     *
     * @param bookID the book ID
     * @param noteID the note ID being replied to
     * @return configured dialog instance
     */
    public static AddReplyDialog forIndependentNote(Long bookID, Long noteID) {
        AddReplyDialog dialog = new AddReplyDialog(new IndependentContext(bookID, noteID));
        dialog.initializeUI();
        return dialog;
    }

    // ==================== UI CONSTRUCTION ====================

    @Override
    protected Node createFormContent() {
        VBox form = new VBox(15);

        // Reply content
        Label contentLabel = new Label("Reply:");
        contentLabel.getStyleClass().add("label");

        contentArea = new TextArea();
        contentArea.setPromptText("Write your reply...");
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(4);
        contentArea.getStyleClass().add("text-input");

        form.getChildren().addAll(contentLabel, contentArea);
        return form;
    }

    @Override
    protected void setupFormValidation() {
        // Enable submit button only when content is not empty
        contentArea.textProperty().addListener((obs, old, val) ->
                updateSubmitButtonState()
        );
    }

    @Override
    protected boolean isFormValid() {
        return contentArea != null &&
                !contentArea.getText().trim().isEmpty();
    }

    @Override
    protected boolean validateForm() {
        String content = contentArea.getText().trim();

        if (content.isEmpty()) {
            showError("Reply content cannot be empty");
            return false;
        }

        if (content.length() > 5000) {
            showError("Reply must be 5000 characters or less");
            return false;
        }

        return true;
    }

    // ==================== SUBMISSION ====================

    @Override
    protected void handleAsyncSubmit() {
        String content = contentArea.getText().trim();

        System.out.println("Submitting reply with context: " + context.getClass().getSimpleName());

        // Polymorphic dispatch based on context type
        switch (context) {
            case DiscussionContext dc -> submitDiscussionReply(dc, content);
            case IndependentContext ic -> submitIndependentReply(ic, content);
        }
    }

    /**
     * Submit a reply to a discussion prompt note.
     */
    private void submitDiscussionReply(DiscussionContext ctx, String content) {
        replyService.createDiscussionReply(
                ctx.clubID(),
                ctx.promptID(),
                ctx.noteID(),
                content,
                this::onSubmitSuccess,
                this::onSubmitError
        );
    }

    /**
     * Submit a reply to an independent club note.
     */
    private void submitIndependentReply(IndependentContext ctx, String content) {
        replyService.createIndependentReply(
                ctx.noteID(),
                ctx.bookID(),
                content,
                reply -> {
                    onSubmitSuccess(reply);
                    EventBus.getInstance().emit(EventType.INDEPENDENT_NOTE_REPLIES_ADDED);
                },
                this::onSubmitError
        );
    }

    // ==================== SUCCESS/ERROR HANDLING ====================

    @Override
    protected String getSuccessMessage(Reply result) {
        return "Reply added successfully!";
    }

    @Override
    protected double getSuccessCloseDelay() {
        return 1.0;
    }
}