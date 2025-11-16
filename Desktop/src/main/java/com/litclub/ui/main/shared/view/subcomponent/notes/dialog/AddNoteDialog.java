package com.litclub.ui.main.shared.view.subcomponent.notes.dialog;

import com.litclub.construct.Book;
import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.interfaces.note.NoteCreateRequest;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.NoteService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AddNoteDialog extends Dialog<NoteCreateRequest> {

    private final LibraryService libraryService;
    private final NoteService noteService;
    private final AppSession session = AppSession.getInstance();

    private final boolean isPersonal;
    private DiscussionPrompt prompt;
    private ComboBox<String> booksComboBox;
    private TextArea noteTextArea;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    private final Button addButton;
    private final Button cancelButton;

    private boolean isSubmitting = false;

    public AddNoteDialog(LibraryService libraryService,
                         NoteService noteService,
                         boolean isPersonal) {

        this.libraryService = libraryService;
        this.noteService = noteService;
        this.isPersonal = isPersonal;

        booksComboBox = new ComboBox<>();
        booksComboBox.getItems().addAll(
                libraryService.getCurrentlyReading().stream()
                        .map(Book::getTitle)
                        .toList()
        );

        if (isPersonal) {
            setTitle("Add personal Note.");
        } else {
            setTitle("Add " + session.getCurrentClub().getClubName() + " Club Note.");
        }

        setResizable(true);

        // Dialog buttons
        ButtonType addButtonType = new ButtonType("Add Book", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = createContent();
        getDialogPane().setContent(content);

        // Button references
        addButton = (Button) getDialogPane().lookupButton(addButtonType);
        cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);

        addButton.setDisable(true);

        // Enable button only when the note text is not empty
        noteTextArea.textProperty().addListener((obs, old, val) ->
                addButton.setDisable(val.trim().isEmpty() || isSubmitting)
        );

        // Prevent dialog from closing on Add click
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isSubmitting) {
                event.consume(); // Prevent default close behavior
                handleSubmit();
            }
        });
    }

    public AddNoteDialog(LibraryService libraryService,
                         NoteService noteService,
                         DiscussionPrompt prompt) {

        this.libraryService = libraryService;
        this.noteService = noteService;
        this.isPersonal = false;
        this.prompt = prompt;

        booksComboBox = new ComboBox<>();
        booksComboBox.getItems().addAll(
                libraryService.getCurrentlyReading().stream()
                        .map(Book::getTitle)
                        .toList()
        );

        setTitle(prompt.getPrompt());

        setResizable(true);

        // Dialog buttons
        ButtonType addButtonType = new ButtonType("Add Book", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox content = createContent();
        getDialogPane().setContent(content);

        // Button references
        addButton = (Button) getDialogPane().lookupButton(addButtonType);
        cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);

        addButton.setDisable(true);

        // Enable button only when the note text is not empty
        noteTextArea.textProperty().addListener((obs, old, val) ->
                addButton.setDisable(val.trim().isEmpty() || isSubmitting)
        );

        // Prevent dialog from closing on Add click
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isSubmitting) {
                event.consume(); // Prevent default close behavior
                handleSubmit();
            }
        });
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.getStyleClass().add("container");
        container.setPadding(new Insets(20));
        container.setMinWidth(500);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Book dropdown
        Label bookLabel = new Label("Book:");
        bookLabel.getStyleClass().add("label");
        grid.add(bookLabel, 0, row);
        grid.add(booksComboBox, 1, row);
        row++;

        // Club label (only for club notes)
        if (!isPersonal) {
            Label clubLabel = new Label("Club:");
            clubLabel.getStyleClass().add("label");

            Label clubName = new Label(session.getCurrentClub().getClubName());
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

        // Status label
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(450);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(30, 30);
        loadingIndicator.setVisible(false);

        container.getChildren().addAll(grid, statusLabel, loadingIndicator);
        container.getStyleClass().add("card");

        return container;
    }

    private void handleSubmit() {
        if (isSubmitting) return;

        String selectedTitle = booksComboBox.getValue();
        String note = noteTextArea.getText().trim();

        // Validate ComboBox
        if (selectedTitle == null) {
            statusLabel.setText("Please select a book.");
            statusLabel.setVisible(true);
            return;
        }

        // Validate Note
        if (note.isEmpty()) {
            statusLabel.setText("Note cannot be empty.");
            statusLabel.setVisible(true);
            return;
        }

        isSubmitting = true;
        addButton.setDisable(true);
        cancelButton.setDisable(true);
        loadingIndicator.setVisible(true);
        statusLabel.setVisible(false);

        // Find book by title
        Book selectedBook = libraryService.getAllBooks().stream()
                .filter(b -> b.getTitle().equals(selectedTitle))
                .findFirst()
                .orElse(null);

        if (selectedBook == null) {
            statusLabel.setText("Could not find selected book.");
            statusLabel.setVisible(true);
            resetSubmittingState();
            return;
        }

        // Build request
        NoteCreateRequest createRequest = new NoteCreateRequest(
                selectedBook.getBookID(),
                isPersonal ? null : session.getCurrentClub().getClubID(),
                note,
                isPersonal
        );

        submitNote(createRequest);
    }

    private void submitNote(NoteCreateRequest createRequest) {
        loadingIndicator.setVisible(true);
        statusLabel.setVisible(false);

        System.out.println("Submitting note...");

        if (isPersonal) {
            noteService.createPersonalNote(
                    session.getUserRecord().userID(),
                    createRequest,
                    note -> Platform.runLater(() -> {
                        showSuccess();

                        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(1));
                        pause.setOnFinished(event -> {
                            setResult(createRequest);
                            close();
                        });
                        pause.play();
                    }),
                    errorMessage -> Platform.runLater(() -> {
                        System.err.println("Failed to add note: " + errorMessage);
                        loadingIndicator.setVisible(false);
                        resetSubmittingState();
                        showError(errorMessage);
                    })
            );
        } else if (prompt != null) {
            noteService.createPromptNote(
                    session.getCurrentClub().getClubID(),
                    prompt.getPromptID(),
                    createRequest,
                    note -> Platform.runLater(() -> {
                        showSuccess();

                        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(1));
                        pause.setOnFinished(event -> {
                            setResult(createRequest);
                            close();
                        });
                        pause.play();
                    }),
                    errorMessage -> Platform.runLater(() -> {
                        System.err.println("Failed to add note: " + errorMessage);
                        loadingIndicator.setVisible(false);
                        resetSubmittingState();
                        showError(errorMessage);
                    })
            );
        } else {
            noteService.createClubNote(
                    session.getCurrentClub().getClubID(),
                    createRequest,
                    note -> Platform.runLater(() -> {
                        showSuccess();

                        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(1));
                        pause.setOnFinished(event -> {
                            setResult(createRequest);
                            close();
                        });
                        pause.play();
                    }),
                    errorMessage -> Platform.runLater(() -> {
                        System.err.println("Failed to add note: " + errorMessage);
                        loadingIndicator.setVisible(false);
                        resetSubmittingState();
                        showError(errorMessage);
                    })
            );
        }
    }

    private void showSuccess() {
        statusLabel.setText("Note added successfully!");
        statusLabel.getStyleClass().removeAll("error-label");
        statusLabel.getStyleClass().add("success-label");
        statusLabel.setVisible(true);
        cancelButton.setDisable(true);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label");
        statusLabel.getStyleClass().add("error-label");
        statusLabel.setVisible(true);
    }

    private void resetSubmittingState() {
        isSubmitting = false;
        addButton.setDisable(false);
        cancelButton.setDisable(false);
    }
}