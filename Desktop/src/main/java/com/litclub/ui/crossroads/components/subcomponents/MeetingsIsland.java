package com.litclub.ui.crossroads.components.subcomponents;

import com.litclub.construct.Meeting;
import com.litclub.theme.ThemeManager;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Displays a preview of upcoming meetings (max 3).
 * Shows meeting title, date/time in a compact card format.
 */
public class MeetingsIsland extends VBox {

    private static final int MAX_MEETINGS = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private final ObservableList<Meeting> meetings;

    public MeetingsIsland(ObservableList<Meeting> meetings) {
        this.meetings = meetings;
        ThemeManager.getInstance().registerComponent(this);

        this.getStyleClass().add("card");
        this.setSpacing(15);
        this.setPadding(new Insets(20));

        showMeetings();

        // Listen for changes in meetings list
        meetings.addListener((javafx.collections.ListChangeListener.Change<? extends Meeting> c) -> {
            refreshMeetings();
        });
    }

    private void showMeetings() {
        // Header
        Label headerLabel = new Label("Upcoming Meetings");
        headerLabel.getStyleClass().add("section-header");
        this.getChildren().add(headerLabel);

        // Check if empty
        if (meetings == null || meetings.isEmpty()) {
            Label emptyLabel = new Label("No upcoming meetings");
            emptyLabel.getStyleClass().add("empty-state");
            emptyLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            this.getChildren().add(emptyLabel);
            return;
        }

        // Get next 2-3 meetings sorted by date
        List<Meeting> upcomingMeetings = meetings.stream()
                .filter(meeting -> meeting.getStartTime() != null && meeting.getStartTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .limit(MAX_MEETINGS)
                .toList();

        if (upcomingMeetings.isEmpty()) {
            Label emptyLabel = new Label("No upcoming meetings");
            emptyLabel.getStyleClass().add("empty-state");
            emptyLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            this.getChildren().add(emptyLabel);
            return;
        }

        // Display each meeting
        for (Meeting meeting : upcomingMeetings) {
            VBox meetingCard = createMeetingCard(meeting);
            this.getChildren().add(meetingCard);
        }
    }

    private VBox createMeetingCard(Meeting meeting) {
        VBox card = new VBox(8);
        card.getStyleClass().add("meeting-preview");
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: rgba(0, 0, 0, 0.05); -fx-background-radius: 8;");

        // Meeting title
        Label titleLabel = new Label(meeting.getTitle());
        titleLabel.getStyleClass().add("meeting-title");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        titleLabel.setWrapText(true);

        // Date and time row
        HBox dateTimeRow = new HBox(10);
        dateTimeRow.setAlignment(Pos.CENTER_LEFT);

        // Date
        Label dateLabel = new Label(meeting.getStartTime().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("meeting-date");
        dateLabel.setStyle("-fx-font-size: 12px;");

        // Time
        Label timeLabel = new Label(meeting.getStartTime().format(TIME_FORMATTER));
        timeLabel.getStyleClass().add("meeting-time");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        dateTimeRow.getChildren().addAll(dateLabel, createDotSeparator(), timeLabel);

        // Location/type if available
        if (meeting.getLocation() != null && !meeting.getLocation().isEmpty()) {
            Label locationLabel = new Label(meeting.getLocation());
            locationLabel.getStyleClass().add("meeting-location");
            locationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray; -fx-font-style: italic;");
            locationLabel.setWrapText(true);
            card.getChildren().addAll(titleLabel, dateTimeRow, locationLabel);
        } else {
            card.getChildren().addAll(titleLabel, dateTimeRow);
        }

        return card;
    }

    /**
     * Creates a small dot separator between date and time.
     */
    private Label createDotSeparator() {
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        return dot;
    }

    /**
     * Refreshes the meetings display when the observable list changes.
     */
    private void refreshMeetings() {
        this.getChildren().clear();
        showMeetings();
    }

    public void refresh() {
        refreshMeetings();
    }

    /**
     * Calculates relative time until meeting (e.g., "in 2 hours", "tomorrow").
     * Optional enhancement for more dynamic display.
     */
    private String getRelativeTime(LocalDateTime meetingTime) {
        LocalDateTime now = LocalDateTime.now();
        long hoursUntil = java.time.Duration.between(now, meetingTime).toHours();

        if (hoursUntil < 1) {
            long minutesUntil = java.time.Duration.between(now, meetingTime).toMinutes();
            return "in " + minutesUntil + " minutes";
        } else if (hoursUntil < 24) {
            return "in " + hoursUntil + " hours";
        } else if (hoursUntil < 48) {
            return "tomorrow";
        } else {
            long daysUntil = java.time.Duration.between(now, meetingTime).toDays();
            return "in " + daysUntil + " days";
        }
    }
}