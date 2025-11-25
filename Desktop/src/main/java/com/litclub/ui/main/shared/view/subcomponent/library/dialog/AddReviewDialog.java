package com.litclub.ui.main.shared.view.subcomponent.library.dialog;

import com.litclub.construct.Review;
import com.litclub.construct.interfaces.library.ReviewRequest;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import com.litclub.ui.main.shared.view.service.ReviewService;
import com.litclub.ui.main.shared.view.subcomponent.library.dialog.subcomponent.StarRater;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

public class AddReviewDialog extends BaseAsyncDialog<Review> {

    private final ReviewService reviewService;
    private final Long bookID;

    private StarRater starRater;
    private TextArea reviewContent;

    public AddReviewDialog(Long bookID, String bookTitle, ReviewService reviewService) {
        super("Add Book Review", "Add Review", 2.0);
        this.reviewService = reviewService;
        this.bookID = bookID;
        setHeaderText("Reviewing " + bookTitle);
        initializeUI();
    }

    @Override
    protected Node createFormContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label ratingLabel = new Label("Rating:");
        ratingLabel.getStyleClass().add("label");
        starRater = new StarRater();
        grid.add(ratingLabel, 0, 0);
        grid.add(starRater, 1, 0);

        Label contentLabel = new Label("Content:");
        contentLabel.getStyleClass().add("label");
        reviewContent = new TextArea();
        reviewContent.getStyleClass().add("text-input");
        grid.add(contentLabel, 0, 1);
        grid.add(reviewContent, 1, 1);

        return grid;
    }

    @Override
    protected void handleAsyncSubmit() {
        Long userID = AppSession.getInstance().getUserRecord().userID();
        ReviewRequest request = new ReviewRequest(
                starRater.getRating(),
                reviewContent.getText()
        );
        System.out.println("Submitting Review...");
        reviewService.addReview(
                userID,
                bookID,
                request,
                // success
                review -> {
                    onSubmitSuccess(review);
                    EventBus.getInstance().emit(EventType.PERSONAL_REVIEWS_UPDATED);
                },
                // error
                this::onSubmitError
        );
    }

    @Override
    protected void setupFormValidation() {
        // Enable submit button only when required fields are filled
        reviewContent.textProperty().addListener((obs, old, val) ->
                updateSubmitButtonState()
        );
    }

    @Override
    protected boolean isFormValid() {
        return reviewContent != null &&
                !reviewContent.getText().trim().isEmpty();
    }

    @Override
    protected boolean validateForm() {
        if (reviewContent.getText().trim().isEmpty()) {
            showError("Title is required");
            return false;
        }
        return true;
    }
}
