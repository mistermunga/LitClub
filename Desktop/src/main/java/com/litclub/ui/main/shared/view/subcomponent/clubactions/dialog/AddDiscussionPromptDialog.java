package com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog;

import com.litclub.construct.Club;
import com.litclub.construct.DiscussionPrompt;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.DiscussionService;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;

public class AddDiscussionPromptDialog extends BaseAsyncDialog<DiscussionPrompt> {

    private final DiscussionService discussionService;
    private final Club club = AppSession.getInstance().getCurrentClub();
    private TextArea promptContent;

    public AddDiscussionPromptDialog() {
        super("Add Discussion Prompt", "Create Prompt");
        discussionService = new DiscussionService();
        setHeaderText("Creating a discussion for " + club.getClubName());
    }

    @Override
    protected Node createFormContent() {
        HBox container = new HBox(12);
        container.getStyleClass().add("container");

        Label contentLabel = new Label("Prompt:");
        contentLabel.getStyleClass().add("label");

        promptContent = new TextArea();
        promptContent.getStyleClass().add("text-input");

        container.getChildren().addAll(contentLabel, promptContent);
        return container;
    }

    @Override
    protected void handleAsyncSubmit() {
        Long clubID = club.getClubID();
        String prompt = promptContent.getText();

        discussionService.createPrompt(
                clubID,
                prompt,
                // success
                this::onSubmitSuccess,
                // fail
                this::onSubmitError
        );
    }

    @Override
    protected void setupFormValidation() {
        promptContent.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSubmitButtonState();
        });
    }

    @Override
    protected boolean validateForm() {
        if (!isFormValid()) {
            showError("Prompt Content cannot be empty");
            return false;
        }
        return true;
    }

    @Override
    protected boolean isFormValid() {
        return !promptContent.getText().isEmpty();
    }

}
