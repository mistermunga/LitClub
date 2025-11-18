package com.litclub.ui.main.shared.view.service;

import com.litclub.construct.interfaces.club.Invite;
import com.litclub.persistence.repository.ClubRepository;
import com.litclub.session.AppSession;

public class ClubService {

    private final ClubRepository clubRepository;
    private final AppSession session;

    public ClubService() {
        this.clubRepository = ClubRepository.getInstance();
        this.session = AppSession.getInstance();
    }

    public Invite generateInvite() {
        return clubRepository.generateInvite(session.getCurrentClub().getClubID()).join();
    }
}
