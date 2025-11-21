package com.litclub.ui.main.shared.view.subcomponent.library.dialog;

import com.litclub.construct.enums.BookStatus;
import com.litclub.construct.interfaces.library.BookAddRequest;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import com.litclub.ui.main.shared.view.service.LibraryService;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Dialog for adding a new book to the library.
 *
 * <p>Collects book details (title, author, ISBN) and initial reading status.
 * Notes field is optional for personal annotations.
 */
public class AddBookDialog extends BaseAsyncDialog<BookData> {

    private final LibraryService libraryService;

    private TextField titleField;
    private TextField authorField;
    private TextField isbnField;
    private ComboBox<BookStatus> statusComboBox;
    private TextArea notesArea;

    public AddBookDialog(LibraryService libraryService) {
        super("Add Book to Library", "Add Book");
        this.libraryService = libraryService;
        setHeaderText("Enter Book Details");
        initializeUI();
    }

    @Override
    protected Node createFormContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        // Title (required)
        Label titleLabel = new Label("Title*:");
        titleLabel.getStyleClass().add("label");
        titleField = new TextField();
        titleField.setPromptText("The Great Gatsby");
        titleField.getStyleClass().add("text-input");
        grid.add(titleLabel, 0, row);
        grid.add(titleField, 1, row);
        row++;

        // Author
        Label authorLabel = new Label("Author:");
        authorLabel.getStyleClass().add("label");
        authorField = new TextField();
        authorField.setPromptText("F. Scott Fitzgerald");
        authorField.getStyleClass().add("text-input");
        grid.add(authorLabel, 0, row);
        grid.add(authorField, 1, row);
        row++;

        // ISBN
        Label isbnLabel = new Label("ISBN:");
        isbnLabel.getStyleClass().add("label");
        isbnField = new TextField();
        isbnField.setPromptText("978-0-7432-7356-5");
        isbnField.getStyleClass().add("text-input");
        grid.add(isbnLabel, 0, row);
        grid.add(isbnField, 1, row);
        row++;

        // Reading Status
        Label statusComboLabel = new Label("Status*:");
        statusComboLabel.getStyleClass().add("label");
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(
                BookStatus.WANT_TO_READ,
                BookStatus.READING,
                BookStatus.READ,
                BookStatus.DNF
        );
        statusComboBox.setValue(BookStatus.WANT_TO_READ);
        statusComboBox.getStyleClass().add("text-input");
        grid.add(statusComboLabel, 0, row);
        grid.add(statusComboBox, 1, row);
        row++;

        // Notes (optional)
        Label notesLabel = new Label("Notes:");
        notesLabel.getStyleClass().add("label");
        notesArea = new TextArea();
        notesArea.setPromptText("Add personal notes about this book...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        notesArea.getStyleClass().add("text-input");
        grid.add(notesLabel, 0, row);
        grid.add(notesArea, 1, row);

        return grid;
    }

    @Override
    protected void setupFormValidation() {
        // Enable submit button only when required fields are filled
        titleField.textProperty().addListener((obs, old, val) ->
                updateSubmitButtonState()
        );
    }

    @Override
    protected boolean isFormValid() {
        return titleField != null &&
                !titleField.getText().trim().isEmpty();
    }

    @Override
    protected boolean validateForm() {
        if (titleField.getText().trim().isEmpty()) {
            showError("Title is required");
            return false;
        }

        return true;
    }

    @Override
    protected void handleAsyncSubmit() {
        Long userID = AppSession.getInstance().getUserRecord().userID();

        // Create BookData record
        BookData bookData = new BookData(
                titleField.getText().trim(),
                authorField.getText().trim(),
                isbnField.getText().trim(),
                statusComboBox.getValue(),
                notesArea.getText().trim()
        );

        // Create API request
        BookAddRequest request = new BookAddRequest(
                bookData.title(),
                bookData.author(),
                bookData.isbn(),
                bookData.status()
        );

        System.out.println("Submitting book: " + bookData.title());

        libraryService.addBook(
                userID,
                request,
                // Success - return BookData to dialog result
                _ -> onSubmitSuccess(bookData),
                // Error
                this::onSubmitError
        );
    }

    @Override
    protected String getSuccessMessage(BookData result) {
        return "Book added successfully!";
    }

    @Override
    protected double getSuccessCloseDelay() {
        return 1.0;
    }
}