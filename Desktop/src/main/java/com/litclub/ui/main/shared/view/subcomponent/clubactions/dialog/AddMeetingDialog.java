package com.litclub.ui.main.shared.view.subcomponent.clubactions.dialog;

import com.litclub.construct.Meeting;
import com.litclub.construct.interfaces.meeting.MeetingCreateRequest;
import com.litclub.session.AppSession;
import com.litclub.ui.main.shared.view.service.MeetingService;
import com.litclub.ui.main.shared.view.subcomponent.clubactions.subcomponents.Scheduler;
import com.litclub.ui.main.shared.view.subcomponent.common.BaseAsyncDialog;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;

public class AddMeetingDialog extends BaseAsyncDialog<Meeting> {

    private final MeetingService meetingService = new MeetingService();
    private final AppSession session = AppSession.getInstance();
    private final boolean isOnline;

    private TextField titleField;
    private Scheduler startTimeScheduler;
    private Scheduler endTimeScheduler;
    private TextField locationField;
    private TextField linkField;

    public AddMeetingDialog(boolean online) {
        super("Schedule new meeting", "Create Meeting");
        isOnline = online;
        setHeaderText("Add Meeting Details");
        initializeUI();
    }

    @Override
    protected Node createFormContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        int row = 0;

        Label titleLabel = new Label("Title*:");
        titleLabel.getStyleClass().add("label");
        titleField = new TextField();
        titleField.setPromptText("Meeting Agenda");
        titleField.getStyleClass().add("text-input");
        grid.add(titleLabel, 0, row);
        grid.add(titleField, 1, row);
        row++;

        Label startTimeLabel = new Label("Start Time: ");
        startTimeLabel.getStyleClass().add("label");
        startTimeScheduler = new Scheduler();
        grid.add(startTimeLabel, 0, row);
        grid.add(startTimeScheduler, 1, row);
        row++;

        Label endTimeLabel = new Label("End Time: ");
        endTimeLabel.getStyleClass().add("label");
        endTimeScheduler = new Scheduler(LocalDateTime.now().plusHours(1));
        grid.add(endTimeLabel, 0, row);
        grid.add(endTimeScheduler, 1, row);
        row++;

        if (isOnline) {
            Label linkLabel = new Label("Link: ");
            linkLabel.getStyleClass().add("label");
            linkField = new TextField();
            linkField.setPromptText("meet.litclub.com");
            linkField.getStyleClass().add("text-input");
            grid.add(linkLabel, 0, row);
            grid.add(linkField, 1, row);
        } else {
            Label locationLabel = new Label("Location: ");
            locationLabel.getStyleClass().add("label");
            locationField = new TextField();
            locationField.setPromptText("Litclub Hall");
            locationField.getStyleClass().add("text-input");
            grid.add(locationLabel, 0, row);
            grid.add(locationField, 1, row);
        }

        return grid;
    }

    @Override
    protected boolean validateForm() {
        if (titleField.getText().isEmpty()) {
            showError("Please enter meeting title");
            return false;
        } else if (startTimeScheduler.getValue() == null || startTimeScheduler.isBefore(LocalDateTime.now())) {
            showError("Start Time is invalid");
            return false;
        } else if (isOnline && linkField.getText().isEmpty()) {
            showError("Please enter meeting link");
            return false;
        } else if (!isOnline && locationField.getText().isEmpty()) {
            showError("Please enter meeting location");
            return false;
        }
        return true;
    }

    @Override
    protected void handleAsyncSubmit() {
        MeetingCreateRequest meetingCreateRequest = new MeetingCreateRequest(
                titleField.getText(),
                startTimeScheduler.getValue(),
                endTimeScheduler.getValue() == null ? LocalDateTime.now().plusMinutes(90) : endTimeScheduler.getValue(),
                isOnline ? null : locationField.getText(),
                isOnline ? linkField.getText() : null
        );

        meetingService.addMeeting(
                meetingCreateRequest,
                this::onSubmitSuccess,
                this::onSubmitError
        );
    }

}
