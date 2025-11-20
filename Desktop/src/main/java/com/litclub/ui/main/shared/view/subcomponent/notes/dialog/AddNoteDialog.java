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

public class AddNoteDialog extends BaseAsyncDialog<Note> {

    // services and session (assigned in constructor)
    private LibraryService libraryService;
    private NoteService noteService;
    private AppSession session;

    // mode
    private boolean isPersonal;
    private DiscussionPrompt prompt;

    // form controls (created in createFormContent)
    private ComboBox<String> booksComboBox;
    private TextArea noteTextArea;

    // -----------------------
    // Constructors
    // -----------------------

    public AddNoteDialog(LibraryService libraryService,
                         NoteService noteService,
                         boolean isPersonal) {
        super("", "Add");

        this.libraryService = libraryService;
        this.noteService = noteService;
        this.session = AppSession.getInstance();
        this.isPersonal = isPersonal;

        setTitle(isPersonal ? "Add personal Note." : "Add " + session.getCurrentClub().getClubName() + " Club Note.");

        initializeBooksDropdown();

        updateSubmitButtonState();
    }

    public AddNoteDialog(LibraryService libraryService,
                         NoteService noteService,
                         DiscussionPrompt prompt) {
        super("", "Add");

        this.libraryService = libraryService;
        this.noteService = noteService;
        this.session = AppSession.getInstance();
        this.isPersonal = false;
        this.prompt = prompt;

        setTitle(prompt.getPrompt());

        initializeBooksDropdown();

        updateSubmitButtonState();
    }

    // -----------------------
    // Abstract implementations from BaseAsyncDialog
    // -----------------------

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
        grid.add(bookLabel, 0, row);
        grid.add(booksComboBox, 1, row);
        row++;

        // Club label (only for club notes)
        if (!isPersonal) {
            Label clubLabel = new Label("Club:");
            clubLabel.getStyleClass().add("label");

            Label clubName = new Label(session != null && session.getCurrentClub() != null ? session.getCurrentClub().getClubName() : "");
            clubName.getStyleClass().add("label");

            grid.add(clubLabel, 0, row);
            grid.add(clubName, 1, row);
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

        // hide any previous error
        hideStatus();
        return true;
    }

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
                isPersonal ? null : session.getCurrentClub().getClubID(),
                noteText,
                isPersonal
        );

        // perform correct service call depending on mode
        if (isPersonal) {
            noteService.createPersonalNote(
                    session.getUserRecord().userID(),
                    createRequest,
                    createdNote -> onSubmitSuccess(createdNote),
                    this::onSubmitError
            );
        } else if (prompt != null) {
            noteService.createPromptNote(
                    session.getCurrentClub().getClubID(),
                    prompt.getPromptID(),
                    createRequest,
                    createdNote -> onSubmitSuccess(createdNote),
                    this::onSubmitError
            );
        } else {
            noteService.createClubNote(
                    session.getCurrentClub().getClubID(),
                    createRequest,
                    createdNote -> onSubmitSuccess(createdNote),
                    this::onSubmitError
            );
        }
    }

    // Optional hooks to wire up UI -> validation
    @Override
    protected void setupFormValidation() {
        // enable/disable submit when note text changes or book selection changes
        noteTextArea.textProperty().addListener((obs, oldV, newV) -> updateSubmitButtonState());
        booksComboBox.valueProperty().addListener((obs, oldV, newV) -> updateSubmitButtonState());
    }

    @Override
    protected boolean isFormValid() {
        String selected = booksComboBox == null ? null : booksComboBox.getValue();
        String text = noteTextArea == null ? "" : noteTextArea.getText();
        return selected != null && !selected.isEmpty() && text != null && !text.trim().isEmpty();
    }

    @Override
    protected String getSuccessMessage(Note result) {
        return "Note added successfully!";
    }

    // -----------------------
    // Helpers
    // -----------------------

    private void initializeBooksDropdown() {
        if (libraryService == null || booksComboBox == null) return;
        booksComboBox.getItems().clear();
        booksComboBox.getItems().addAll(
                libraryService.getCurrentlyReading().stream()
                        .map(Book::getTitle)
                        .toList()
        );
    }
}
