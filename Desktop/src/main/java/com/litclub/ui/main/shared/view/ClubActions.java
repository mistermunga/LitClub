package com.litclub.ui.main.shared.view;

import com.litclub.construct.enums.ClubRole;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.ClubBookService;
import com.litclub.ui.main.shared.view.service.ClubService;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog.AddClubBookDialog;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog.AddDiscussionPromptDialog;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog.AddMeetingDialog;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ClubActions extends VBox {

    private final ClubService clubService;
    private final ClubBookService clubBookService;

    public ClubActions() {
        this.setSpacing(12);
        this.getStyleClass().add("club-actions");
        this.clubService = new ClubService();
        this.clubBookService = new ClubBookService();

        ClubRole highestRole = AppSession.getInstance().getHighestRole();
        Node content = switch (highestRole) {
            case MODERATOR -> createModeratorUI();
            case OWNER -> createOwnerUI();
            default -> createDefaultUI();
        };

        // Wrap UI inside a container Node
        TitledPane container = new TitledPane("Club Actions", content);
        container.setCollapsible(false);

        getChildren().add(container);
    }

    private Node createModeratorUI() {
        VBox box = new VBox(8);
        box.getChildren().add(new Label("Moderator Tools"));

        Button getInviteButton = new Button("Generate Invite");
        Label codeLabel = new Label();

        getInviteButton.setOnAction(e -> {
            String code = clubService.generateInvite().invite();
            codeLabel.setText(code);
            System.out.println("Generated Invite: " + code);
        });

        Button createDiscussionButton = new Button("Create Discussion");
        createDiscussionButton.setOnAction(e -> createDiscussionPrompt());

        box.getChildren().addAll(getInviteButton, codeLabel, createDiscussionButton);

        HBox meetingBox = new HBox();
        Label meetingLabel = new Label("Add Meeting");
        Button offlineMeetingButton = new Button("Offline Meeting");
        Button onlineMeetingButton = new Button("Physical Meeting");
        offlineMeetingButton.setOnAction(e -> createMeetingDialog(false));
        onlineMeetingButton.setOnAction(e -> createMeetingDialog(true));
        meetingBox.getChildren().addAll(meetingLabel, offlineMeetingButton, onlineMeetingButton);

        box.getChildren().add(meetingBox);

        // TODO add moderator-specific buttons, etc

        return box;
    }

    private void createDiscussionPrompt() {
        AddDiscussionPromptDialog promptDialog = new AddDiscussionPromptDialog();
        promptDialog.showAndWait().ifPresent(prompt -> {
            System.out.println("Added Discussion Prompt: " + prompt.getPrompt());
        });
    }

    private Node createOwnerUI() {
        VBox box = new VBox(10);

        // Reuse moderator UI inside owner UI
        box.getChildren().add(createModeratorUI());

        box.getChildren().add(new Label("Owner Tools"));
        // TODO add owner-specific controls
        Button clubBookButton = new Button("Add new club book");
        clubBookButton.setOnAction(e -> createClubBookDialog());
        box.getChildren().add(clubBookButton);

        return box;
    }

    private void createClubBookDialog() {
        AddClubBookDialog bookDialog = new AddClubBookDialog();

        bookDialog.showAndWait().ifPresent(book -> {
            System.out.println("Set Club Book to " + book.getTitle());
        });
    }

    private void createMeetingDialog(boolean isOnline) {
        AddMeetingDialog meetingDialog = new AddMeetingDialog(isOnline);
        meetingDialog.showAndWait().ifPresent(meeting -> {
            System.out.println("Added Meeting " + meeting.getTitle());
        });
    }

    private Node createDefaultUI() {
        return new Label("No actions available");
    }
}
