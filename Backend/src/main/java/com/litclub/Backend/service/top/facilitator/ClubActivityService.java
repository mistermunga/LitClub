package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.construct.club.*;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.MeetingService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ClubActivityService {

    private final ClubService clubService;
    private final UserService userService;
    private final MeetingService meetingService;
    private final DiscussionPromptService promptService;
    private final NoteService noteService;
    private final ClubMembershipService membershipService;
    private final BookService bookService;

    public ClubActivityService(
            ClubService clubService,
            UserService userService,
            MeetingService meetingService,
            DiscussionPromptService promptService,
            NoteService noteService,
            ClubMembershipService membershipService, BookService bookService) {
        this.clubService = clubService;
        this.userService = userService;
        this.meetingService = meetingService;
        this.promptService = promptService;
        this.noteService = noteService;
        this.membershipService = membershipService;
        this.bookService = bookService;
    }

    // ====== CLUB OVERVIEW ======
    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public ClubActivityReport getClubActivity(Long clubID) {
        Club club = clubService.requireClubById(clubID);

        List<Meeting> meetings = meetingService.getMeetingsForClub(club);
        List<Meeting> upcomingMeetings = filterUpcomingMeetings(meetings);
        List<Meeting> pastMeetings = filterPastMeetings(meetings);

        List<DiscussionPrompt> prompts = promptService.findAllPromptsByClub(club);
        List<Note> notes = noteService.getAllNotes(club);

        List<User> members = clubService.getUsersForClub(club);

        return new ClubActivityReport(
                clubService.convertClubToDTO(club),
                upcomingMeetings,
                pastMeetings,
                prompts,
                notes,
                members.size()
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public ClubDashboard getClubDashboard(Long clubID) {
        Club club = clubService.requireClubById(clubID);

        ClubActivityReport activityReport = getClubActivity(club.getClubID());

        return new ClubDashboard(
                activityReport.club(),
                activityReport.totalMembers(),
                activityReport.upcomingMeetings().size(),
                activityReport.activePrompts().size(),
                clubService.getUsersForClub(club).stream().map(UserService::convertUserToRecord).toList(),
                activityReport.upcomingMeetings()
                        .stream().min(Comparator.comparing(Meeting::getStartTime))
                        .orElse(null)
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public UserClubParticipation getUserParticipation(Long clubID, Long userID) {
        User user = userService.requireUserById(userID);
        Club club = clubService.requireClubById(clubID);

        return new UserClubParticipation(
                UserService.convertUserToRecord(user),
                clubService.convertClubToDTO(club),
                meetingService.getMeetings(user, club),
                noteService.getAllNotes(user),
                promptService.findAllByPoster(user),
                membershipService.getJoined(club, user)
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public List<MemberParticipation> getMemberParticipations(Long clubID) {
        Club club = clubService.requireClubById(clubID);
        List<User> users = clubService.getUsersForClub(club);
        List<MemberParticipation> memberParticipations = new ArrayList<>();
        for (User user : users) {
            memberParticipations.add(
                    new MemberParticipation(
                            UserService.convertUserToRecord(user),
                            meetingService.getMeetings(user, club).size(),
                            noteService.getAllNotes(user).size(),
                            promptService.findAllByPoster(user).size(),
                            true
            ));
        }
        return memberParticipations;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public List<Meeting> getUpcomingMeetings(Long clubID) {
        List<Meeting> meetings = meetingService.getMeetingsForClub(clubService.requireClubById(clubID));
        return filterUpcomingMeetings(meetings);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public List<DiscussionPrompt> getDiscussionPrompts(Long clubID) {
        Club club = clubService.requireClubById(clubID);
        return promptService.findAllPromptsByClub(club);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public List<Book> getCurrentlyReading(Long clubId) {
        return bookService.getBooksByUser(
                clubService.getClubOwners(clubId).getFirst()
        );
    }

    // Analytics
    @Transactional(readOnly = true)
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public ClubStatistics getClubStatistics(Long clubID) {
        Club club = clubService.requireClubById(clubID);
        return new ClubStatistics(
                clubID,
                clubService.getUsersForClub(club).size(),
                meetingService.getMeetingsForClub(club).size(),
                promptService.findAllPromptsByClub(club).size(),
                noteService.getAllNotes(club).size(),
                bookService.getBooksByUser(clubService.getClubOwners(clubID).getFirst()).size(),
                -1
        );
    }

    // ------ Utility ------
    private static List<Meeting> filterUpcomingMeetings(List<Meeting> meetings) {
        return meetings.stream()
                .filter(meeting -> meeting.getStartTime().isAfter(LocalDateTime.now()))
                .toList();
    }

    private static List<Meeting> filterPastMeetings(List<Meeting> meetings) {
        return meetings.stream()
                .filter(meeting -> meeting.getStartTime().isBefore(LocalDateTime.now()))
                .toList();
    }
}
