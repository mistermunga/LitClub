package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Meeting;
import com.litclub.construct.interfaces.meeting.MeetingCreateRequest;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.session.AppSession;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

public class MeetingService {

    private ObservableList<Meeting> clubMeetings;
    private final ClubRepository clubRepository;
    private final AppSession session;

    public MeetingService() {
        this.clubRepository = ClubRepository.getInstance();
        this.session = AppSession.getInstance();
        loadClubMeetings();
        this.clubMeetings = clubRepository.getMeetings();
    }

    public void loadClubMeetings() {
        clubRepository.fetchClubMeetings(session.getCurrentClub().getClubID());
    }

    public ObservableList<Meeting> getClubMeetings() {
        return clubMeetings;
    }

    public void addMeeting(MeetingCreateRequest createRequest,
                           Consumer<Meeting> onSuccess,
                           Consumer<String> onError) {
        clubRepository.createMeeting(
                session.getCurrentClub().getClubID(),
                createRequest
        ).thenAccept(meeting -> {
            Platform.runLater(() -> {
                System.out.println("Meeting added");
                loadClubMeetings();
                onSuccess.accept(meeting);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                String error = ApiErrorHandler.parseError(throwable);
                System.out.println("Meeting adding failed: " + error);
                onError.accept("Meeting adding failed: " + error);
            });
            return null;
        });
    }
}
