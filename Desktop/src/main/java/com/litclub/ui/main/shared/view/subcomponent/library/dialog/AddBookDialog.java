package com.litclub.ui.main.shared.view.subcomponent.library.dialog;

import com.litclub.construct.enums.BookStatus;
import com.litclub.construct.interfaces.library.BookAddRequest;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.LibraryService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Dialog for adding a new book to the library.
 * Collects book details and initial reading status.
 */
public class AddBookDialog extends Dialog<BookData> {

    private final LibraryService libraryService;

    private TextField titleField;
    private TextField authorField;
    private TextField isbnField;
    private ComboBox<BookStatus> statusComboBox;
    private TextArea notesArea;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    private Button addButton;
    private Button cancelButton;
    private boolean isSubmitting = false;

    public AddBookDialog(LibraryService libraryService) {
        this.libraryService = libraryService;

        setTitle("Add Book to Library");
        setHeaderText("Enter book details");
        setResizable(true);

        // Dialog buttons
        ButtonType addButtonType = new ButtonType("Add Book", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Build UI
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Get references to buttons
        addButton = (Button) getDialogPane().lookupButton(addButtonType);
        cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        addButton.setDisable(true);

        // Enable Add button only when required fields are filled
        titleField.textProperty().addListener((obs, old, val) ->
                addButton.setDisable(val.trim().isEmpty() || isSubmitting)
        );

        // Prevent dialog from closing on button click
        addButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!isSubmitting) {
                event.consume(); // Prevent default close behavior
                handleSubmit();
            }
        });

        // Don't use result converter - handle submission manually
        setResultConverter(buttonType -> {
            // Only return result if explicitly closed (after success)
            return null;
        });
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setMinWidth(500);

        // Form grid
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

        // Status message
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
        return container;
    }

    private void handleSubmit() {
        if (isSubmitting) return;

        isSubmitting = true;
        addButton.setDisable(true);
        cancelButton.setDisable(true);

        BookData bookData = new BookData(
                titleField.getText().trim(),
                authorField.getText().trim(),
                isbnField.getText().trim(),
                statusComboBox.getValue(),
                notesArea.getText().trim()
        );

        submitBook(bookData);
    }

    private void submitBook(BookData bookData) {
        // Show loading state
        loadingIndicator.setVisible(true);
        statusLabel.setVisible(false);

        Long userID = AppSession.getInstance().getUserRecord().userID();

        // Create request
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
                // Success
                bookWithStatus -> {
                    System.out.println("Book successfully added to library: " + bookWithStatus.book().getTitle());
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        showSuccess("Book added successfully!");

                        // Close dialog after short delay
                        javafx.animation.PauseTransition pause =
                                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                        pause.setOnFinished(e -> {
                            setResult(bookData); // Set result before closing
                            close();
                        });
                        pause.play();
                    });
                },
                // Error
                errorMessage -> {
                    System.err.println("Failed to add book: " + errorMessage);
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        isSubmitting = false;
                        addButton.setDisable(false);
                        cancelButton.setDisable(false);
                        showError(errorMessage);
                    });
                }
        );
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label");
        statusLabel.getStyleClass().add("success-label");
        statusLabel.setVisible(true);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("success-label");
        statusLabel.getStyleClass().add("error-label");
        statusLabel.setVisible(true);
    }
}