package com.litclub.ui.main.shared.view.subcomponent.notes.dialog;

import com.litclub.construct.Book;
import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Note;
import com.litclub.construct.interfaces.note.NoteCreateRequest;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.NoteService;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Polymorphic dialog for adding notes.
 * Supports personal notes, club notes, and discussion prompt notes.
 */
public class AddNoteDialog extends BaseAsyncDialog<Note> {

    private final LibraryService libraryService;
    private final NoteService noteService;
    private final AppSession session;
    private final NoteContext context;

    // Form controls
    private ComboBox<String> booksComboBox;
    private TextArea noteTextArea;

    // ==================== CONTEXT CLASSES ====================

    /**
     * Context information for creating a note.
     * Sealed interface with three implementations.
     */
    private sealed interface NoteContext permits PersonalContext, ClubContext, PromptContext {
        boolean isPersonal();
        String getDialogTitle();
    }

    /**
     * Context for personal notes.
     */
    private record PersonalContext(Long userID) implements NoteContext {
        @Override
        public boolean isPersonal() {
            return true;
        }

        @Override
        public String getDialogTitle() {
            return "Add Personal Note";
        }
    }

    /**
     * Context for club notes (not discussion prompts).
     */
    private record ClubContext(Long clubID, String clubName) implements NoteContext {
        @Override
        public boolean isPersonal() {
            return false;
        }

        @Override
        public String getDialogTitle() {
            return "Add " + clubName + " Club Note";
        }
    }

    /**
     * Context for discussion prompt notes.
     */
    private record PromptContext(
            Long clubID,
            Long promptID,
            String promptText
    ) implements NoteContext {
        @Override
        public boolean isPersonal() {
            return false;
        }

        @Override
        public String getDialogTitle() {
            return promptText;
        }
    }

    // ==================== PRIVATE CONSTRUCTOR ====================

    /**
     * Private constructor - use static factory methods instead.
     */
    private AddNoteDialog(
            LibraryService libraryService,
            NoteService noteService,
            NoteContext context
    ) {
        super("", "Add");

        this.libraryService = libraryService;
        this.noteService = noteService;
        this.session = AppSession.getInstance();
        this.context = context;

        setTitle(context.getDialogTitle());
        updateSubmitButtonState();
    }

    // ==================== STATIC FACTORY METHODS ====================

    /**
     * Create dialog for personal notes.
     *
     * @param libraryService the library service
     * @param noteService the note service
     * @return configured dialog instance
     */
    public static AddNoteDialog forPersonalNote(
            LibraryService libraryService,
            NoteService noteService
    ) {
        Long userID = AppSession.getInstance().getUserRecord().userID();
        return new AddNoteDialog(
                libraryService,
                noteService,
                new PersonalContext(userID)
        );
    }

    /**
     * Create dialog for club notes (non-discussion).
     *
     * @param libraryService the library service
     * @param noteService the note service
     * @return configured dialog instance
     */
    public static AddNoteDialog forClubNote(
            LibraryService libraryService,
            NoteService noteService
    ) {
        AppSession session = AppSession.getInstance();
        return new AddNoteDialog(
                libraryService,
                noteService,
                new ClubContext(
                        session.getCurrentClub().getClubID(),
                        session.getCurrentClub().getClubName()
                )
        );
    }

    /**
     * Create dialog for discussion prompt notes.
     *
     * @param libraryService the library service
     * @param noteService the note service
     * @param prompt the discussion prompt
     * @return configured dialog instance
     */
    public static AddNoteDialog forPromptNote(
            LibraryService libraryService,
            NoteService noteService,
            DiscussionPrompt prompt
    ) {
        Long clubID = AppSession.getInstance().getCurrentClub().getClubID();
        return new AddNoteDialog(
                libraryService,
                noteService,
                new PromptContext(clubID, prompt.getPromptID(), prompt.getPrompt())
        );
    }

    // ==================== UI CONSTRUCTION ====================

    @Override
    protected Node createFormContent() {
        VBox container = new VBox(15);
        container.getStyleClass().add("container");
        container.setMinWidth(500);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Book dropdown
        Label bookLabel = new Label("Book:");
        bookLabel.getStyleClass().add("label");

        booksComboBox = new ComboBox<>();
        initializeBooksDropdown();

        grid.add(bookLabel, 0, row);
        grid.add(booksComboBox, 1, row);
        row++;

        // Club label (only for club/prompt notes)
        if (!context.isPersonal()) {
            Label clubLabel = new Label("Club:");
            clubLabel.getStyleClass().add("label");

            String clubName = switch (context) {
                case ClubContext cc -> cc.clubName();
                case PromptContext pc -> session.getCurrentClub().getClubName();
                default -> "";
            };

            Label clubNameLabel = new Label(clubName);
            clubNameLabel.getStyleClass().add("label");

            grid.add(clubLabel, 0, row);
            grid.add(clubNameLabel, 1, row);
            row++;
        }

        // Note field
        Label noteLabel = new Label("Note:");
        noteLabel.getStyleClass().add("label");

        noteTextArea = new TextArea();
        noteTextArea.setPromptText("dear future me, ");
        noteTextArea.getStyleClass().add("text-input");
        noteTextArea.setWrapText(true);
        noteTextArea.setPrefRowCount(3);

        grid.add(noteLabel, 0, row);
        grid.add(noteTextArea, 1, row);

        container.getChildren().addAll(grid);

        return container;
    }

    @Override
    protected void setupFormValidation() {
        // Enable/disable submit when note text or book selection changes
        noteTextArea.textProperty().addListener((obs, oldV, newV) ->
                updateSubmitButtonState());
        booksComboBox.valueProperty().addListener((obs, oldV, newV) ->
                updateSubmitButtonState());
    }

    @Override
    protected boolean isFormValid() {
        String selected = booksComboBox == null ? null : booksComboBox.getValue();
        String text = noteTextArea == null ? "" : noteTextArea.getText();
        return selected != null && !selected.isEmpty() &&
                text != null && !text.trim().isEmpty();
    }

    @Override
    protected boolean validateForm() {
        String selectedTitle = booksComboBox.getValue();
        String note = noteTextArea.getText() == null ? "" : noteTextArea.getText().trim();

        if (selectedTitle == null || selectedTitle.isEmpty()) {
            showError("Please select a book.");
            return false;
        }

        if (note.isEmpty()) {
            showError("Note cannot be empty.");
            return false;
        }

        // Hide any previous error
        hideStatus();
        return true;
    }

    // ==================== SUBMISSION ====================

    @Override
    protected void handleAsyncSubmit() {
        // Build request
        String selectedTitle = booksComboBox.getValue();
        String noteText = noteTextArea.getText().trim();

        Book selectedBook = libraryService.getAllBooks().stream()
                .filter(b -> Objects.equals(b.getTitle(), selectedTitle))
                .findFirst()
                .orElse(null);

        if (selectedBook == null) {
            onSubmitError("Could not find selected book.");
            return;
        }

        NoteCreateRequest createRequest = new NoteCreateRequest(
                selectedBook.getBookID(),
                context.isPersonal() ? null : getCurrentClubID(),
                noteText,
                context.isPersonal()
        );

        // Polymorphic dispatch based on context type
        switch (context) {
            case PersonalContext pc -> submitPersonalNote(pc, createRequest);
            case ClubContext cc -> submitClubNote(cc, createRequest);
            case PromptContext prc -> submitPromptNote(prc, createRequest);
        }
    }

    /**
     * Submit a personal note.
     */
    private void submitPersonalNote(PersonalContext ctx, NoteCreateRequest request) {
        noteService.createPersonalNote(
                ctx.userID(),
                request,
                this::onSubmitSuccess,
                this::onSubmitError
        );
    }

    /**
     * Submit a club note.
     */
    private void submitClubNote(ClubContext ctx, NoteCreateRequest request) {
        noteService.createClubNote(
                ctx.clubID(),
                request,
                this::onSubmitSuccess,
                this::onSubmitError
        );
    }

    /**
     * Submit a prompt note.
     */
    private void submitPromptNote(PromptContext ctx, NoteCreateRequest request) {
        noteService.createPromptNote(
                ctx.clubID(),
                ctx.promptID(),
                request,
                this::onSubmitSuccess,
                this::onSubmitError
        );
    }

    // ==================== HELPERS ====================

    private void initializeBooksDropdown() {
        if (libraryService == null || booksComboBox == null) return;

        booksComboBox.getItems().clear();
        booksComboBox.getItems().addAll(
                libraryService.getCurrentlyReading().stream()
                        .map(Book::getTitle)
                        .toList()
        );
    }

    private Long getCurrentClubID() {
        return switch (context) {
            case ClubContext cc -> cc.clubID();
            case PromptContext prc -> prc.clubID();
            default -> null;
        };
    }

    // ==================== SUCCESS/ERROR HANDLING ====================

    @Override
    protected String getSuccessMessage(Note result) {
        return "Note added successfully!";
    }

    @Override
    protected double getSuccessCloseDelay() {
        return 1.0;
    }
}