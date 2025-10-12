package com.litclub.ui.component.content;

import com.litclub.session.AppSession;
import com.litclub.session.construct.ClubRecord;
import com.litclub.session.construct.MeetingRecord;
import com.litclub.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

public class MeetingsView extends ScrollPane {

    private final VBox container;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy  •  h:mm a");

    public MeetingsView() {
        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("meetings-view", "scroll-pane");
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        // Main container with proper padding
        this.container = new VBox(20);
        this.container.setPadding(new Insets(30, 40, 30, 40));
        this.container.getStyleClass().add("meetings-container");
        VBox.setVgrow(this.container, Priority.ALWAYS);

        // Add header
        addHeader();

        // Add meetings
        showMeetings();

        this.setContent(container);
    }

    private void addHeader() {
        Label headerLabel = new Label("Upcoming Meetings");
        headerLabel.getStyleClass().add("meetings-header");
        container.getChildren().add(headerLabel);
    }

    public void showMeetings() {
        Set<MeetingRecord> meetingRecords = getMeetings();

        // Clear only meeting cards, keep the header
        container.getChildren().removeIf(node ->
                node.getStyleClass().contains("meeting-card")
        );

        if (meetingRecords.isEmpty()) {
            Label emptyState = new Label("No upcoming meetings scheduled");
            emptyState.getStyleClass().add("empty-state");
            container.getChildren().add(emptyState);
            return;
        }

        for (MeetingRecord meetingRecord : meetingRecords) {
            VBox meetingCard = createMeetingCard(meetingRecord);
            container.getChildren().add(meetingCard);
        }
    }

    private VBox createMeetingCard(MeetingRecord meetingRecord) {
        VBox meetingCard = new VBox(12);
        meetingCard.setPadding(new Insets(20, 24, 20, 24));
        meetingCard.getStyleClass().add("meeting-card");

        // Title
        Label title = new Label(meetingRecord.meetingName());
        title.getStyleClass().add("meeting-title");
        title.setWrapText(true);

        // Club name badge
        Label clubName = new Label(meetingRecord.club().name());
        clubName.getStyleClass().add("meeting-club-badge");

        // Time info
        String startTime = meetingRecord.start().format(TIME_FORMATTER);
        String endTime = meetingRecord.end().format(DateTimeFormatter.ofPattern("h:mm a"));
        Label timeLabel = new Label("🕐 " + startTime + " - " + endTime);
        timeLabel.getStyleClass().add("meeting-time");

        // Location info
        Label locationLabel;
        if (meetingRecord.link().isPresent()) {
            locationLabel = new Label("🔗 " + meetingRecord.link().get());
            locationLabel.getStyleClass().addAll("meeting-location", "meeting-location-link");
        } else {
            locationLabel = new Label("📍 " + meetingRecord.location());
            locationLabel.getStyleClass().add("meeting-location");
        }
        locationLabel.setWrapText(true);

        // Info section with time and location
        VBox infoSection = new VBox(8);
        infoSection.getChildren().addAll(timeLabel, locationLabel);
        infoSection.getStyleClass().add("meeting-info-section");

        // Assemble card
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().addAll(title, clubName);

        meetingCard.getChildren().addAll(topRow, infoSection);

        return meetingCard;
    }

    // TODO Fetch meetings for a club -> API Calls
    public Set<MeetingRecord> getMeetings() {
        ClubRecord club = AppSession.getInstance().getClubRecord();
        return Set.of(
                new MeetingRecord(
                        "Poetry Night – Virtual Edition",
                        LocalDateTime.of(2025, 10, 15, 19, 0),
                        LocalDateTime.of(2025, 10, 15, 21, 0),
                        club,
                        "Online",
                        Optional.of("https://meet.litclub.com/poetry-night")
                ),
                new MeetingRecord(
                        "Writers' Workshop – October",
                        LocalDateTime.of(2025, 10, 22, 18, 30),
                        LocalDateTime.of(2025, 10, 22, 20, 30),
                        club,
                        "Online",
                        Optional.of("https://meet.litclub.com/workshop-oct")
                ),
                new MeetingRecord(
                        "Classic Literature Discussion",
                        LocalDateTime.of(2025, 10, 18, 17, 0),
                        LocalDateTime.of(2025, 10, 18, 19, 0),
                        club,
                        "Central Library – Room 204",
                        Optional.empty()
                ),
                new MeetingRecord(
                        "Coffee & Classics Meetup",
                        LocalDateTime.of(2025, 10, 25, 10, 0),
                        LocalDateTime.of(2025, 10, 25, 12, 0),
                        club,
                        "Maple Street Café",
                        Optional.empty()
                )
        );
    }
}