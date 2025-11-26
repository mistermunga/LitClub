package com.litclub.ui.main.shared.view;

import com.litclub.construct.Meeting;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.ui.main.shared.event.EventBus;
import com.litclub.ui.main.shared.event.EventBus.EventType;
import com.litclub.ui.main.shared.view.service.MeetingService;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public class MeetingsView extends ScrollPane {

    private final MeetingService meetingService;
    private ObservableList<Meeting> clubMeetings;

    private final VBox container;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy  •  h:mm a");

    public MeetingsView() {
        meetingService = new MeetingService();

        ThemeManager.getInstance().registerComponent(this);
        this.getStyleClass().addAll("meetings-view", "scroll-pane");
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

        this.container = new VBox(20);
        this.container.setPadding(new Insets(30, 40, 30, 40));
        this.container.getStyleClass().add("meetings-container");
        VBox.setVgrow(this.container, Priority.ALWAYS);

        this.setVvalue(0);
        this.setPannable(false);

        setupSmoothScrolling();

        addHeader();
        showMeetings();

        this.setContent(container);

        EventBus.getInstance().on(
                EventType.CLUB_MEETINGS_UPDATED,
                () -> {
                    ClubRepository.getInstance().fetchClubMeetings(
                            AppSession.getInstance().getCurrentClub().getClubID()
                    );
                });
    }

    private void addHeader() {
        Label headerLabel = new Label("Upcoming Meetings");
        headerLabel.getStyleClass().add("meetings-header");
        container.getChildren().add(headerLabel);
    }

    public void showMeetings() {
        clubMeetings = meetingService.getClubMeetings();
        clubMeetings.sort(Comparator.comparing(Meeting::getStartTime).reversed());

        // Clear only meeting cards, keep the header
        container.getChildren().removeIf(node ->
                node.getStyleClass().contains("meeting-card")
        );

        if (clubMeetings.isEmpty()) {
            Label emptyState = new Label("No upcoming meetings scheduled");
            emptyState.getStyleClass().add("empty-state");
            container.getChildren().add(emptyState);
            return;
        }

        for (Meeting meeting : clubMeetings) {
            VBox card = createMeetingCard(meeting);
            container.getChildren().add(card);
        }
    }

    private VBox createMeetingCard(Meeting meeting) {
        VBox meetingCard = new VBox(12);
        meetingCard.setPadding(new Insets(20, 24, 20, 24));
        meetingCard.getStyleClass().add("meeting-card");

        // Title
        Label title = new Label(meeting.getTitle());
        title.getStyleClass().add("meeting-title");
        title.setWrapText(true);

        // Club name badge
        Label clubName = new Label(AppSession.getInstance().getCurrentClub().getClubName());
        clubName.getStyleClass().add("meeting-club-badge");

        // Time info
        String startTime = meeting.getStartTime().format(TIME_FORMATTER);
        String endTime = meeting.getEndTime().format(DateTimeFormatter.ofPattern("h:mm a"));
        Label timeLabel = new Label("🕐 " + startTime + " - " + endTime);
        timeLabel.getStyleClass().add("meeting-time");

        // Location info
        Label locationLabel;
        if (meeting.getLink() != null || !meeting.getLink().isEmpty()) {
            locationLabel = new Label("🔗 " + meeting.getLink());
            locationLabel.getStyleClass().addAll("meeting-location", "meeting-location-link");
        } else {
            locationLabel = new Label("📍 " + meeting.getLocation());
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

    private void setupSmoothScrolling() {
        final double SPEED = 0.005;
        this.setOnScroll(event -> {
            double deltaY = event.getDeltaY() * SPEED;
            this.setVvalue(this.getVvalue() - deltaY);
        });
    }
}