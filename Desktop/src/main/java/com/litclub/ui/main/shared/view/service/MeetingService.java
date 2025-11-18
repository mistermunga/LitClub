package com.litclub.ui.main.shared.view.service;

import com.litclub.construct.Meeting;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.session.AppSession;
import javafx.collections.ObservableList;

public class MeetingService {

    private ObservableList<Meeting> clubMeetings;
    private final ClubRepository clubRepository;
    private final AppSession session;

    public MeetingService() {
        this.clubRepository = ClubRepository.getInstance();
        this.session = AppSession.getInstance();

        clubRepository.fetchClubMeetings(session.getCurrentClub().getClubID());

        this.clubMeetings = clubRepository.getMeetings();
    }

    public ObservableList<Meeting> getClubMeetings() {
        return clubMeetings;
    }
}
