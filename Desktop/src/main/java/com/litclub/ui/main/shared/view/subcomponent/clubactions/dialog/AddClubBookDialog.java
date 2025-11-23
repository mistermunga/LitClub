package com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog;

import com.litclub.construct.Book;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.LibraryService;
import com.litclub.ui.main.shared.view.service.ClubBookService;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Dialog for adding a book from the owner's currently reading list to the club.
 * Simple selection from a dropdown - owner only.
 */
public class AddClubBookDialog extends BaseAsyncDialog<Book> {

    private final ClubBookService clubBookService;
    private final LibraryService libraryService;

    private ComboBox<Book> bookComboBox;

    public AddClubBookDialog() {
        super("Add Book to Club", "Add Book");
        this.clubBookService = new ClubBookService();
        this.libraryService = new LibraryService();
        setHeaderText("Select a book from your currently reading list");
        initializeUI();
    }

    @Override
    protected Node createFormContent() {
        VBox content = new VBox(15);

        // Book selection
        Label bookLabel = new Label("Book:");
        bookLabel.getStyleClass().add("label");

        bookComboBox = new ComboBox<>();
        bookComboBox.setPromptText("Choose a book...");
        bookComboBox.getStyleClass().add("text-input");
        bookComboBox.setPrefWidth(400);

        // Custom cell factory to show book titles nicely
        bookComboBox.setCellFactory(param -> new BookListCell());
        bookComboBox.setButtonCell(new BookListCell());

        // Load currently reading books
        List<Book> currentlyReading = libraryService.getCurrentlyReading();
        bookComboBox.getItems().addAll(currentlyReading);

        content.getChildren().addAll(bookLabel, bookComboBox);

        return content;
    }

    @Override
    protected void setupFormValidation() {
        bookComboBox.valueProperty().addListener((obs, old, val) ->
                updateSubmitButtonState()
        );
    }

    @Override
    protected boolean isFormValid() {
        return bookComboBox.getValue() != null;
    }

    @Override
    protected boolean validateForm() {
        if (bookComboBox.getValue() == null) {
            showError("Please select a book");
            return false;
        }
        return true;
    }

    @Override
    protected void handleAsyncSubmit() {
        Book selectedBook = bookComboBox.getValue();
        Long clubID = AppSession.getInstance().getCurrentClub().getClubID();

        clubBookService.addBook(
                clubID,
                selectedBook.getBookID(),
                this::onSubmitSuccess,
                this::onSubmitError
        );
    }

    @Override
    protected String getSuccessMessage(Book result) {
        return "\"" + result.getTitle() + "\" added to club!";
    }

    @Override
    protected double getSuccessCloseDelay() {
        return 1.0; // Close after 1 second
    }

    // ==================== CUSTOM CELL ====================

    /**
     * Custom cell for displaying books with title and author
     */
    private static class BookListCell extends ListCell<Book> {
        @Override
        protected void updateItem(Book book, boolean empty) {
            super.updateItem(book, empty);
            if (empty || book == null) {
                setText(null);
            } else {
                String display = book.getTitle();
                if (book.getPrimaryAuthor() != null && !book.getPrimaryAuthor().isEmpty()) {
                    display += " by " + book.getPrimaryAuthor();
                }
                setText(display);
            }
        }
    }
}