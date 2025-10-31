package com.litclub.Backend.service.top.gatekeeper;

import com.litclub.Backend.construct.meeting.MeetingCreateRequest;
import com.litclub.Backend.construct.meeting.MeetingUpdateRequest;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.security.roles.ClubRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.MeetingService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ClubModService {

    private final MeetingService meetingService;
    private final DiscussionPromptService discussionPromptService;
    private final ClubService clubService;
    private final UserService userService;
    private final ClubMembershipService membershipService;

    public ClubModService(
            MeetingService meetingService,
            DiscussionPromptService discussionPromptService,
            ClubService clubService,
            UserService userService,
            ClubMembershipService membershipService
    ) {
        this.meetingService = meetingService;
        this.discussionPromptService = discussionPromptService;
        this.clubService = clubService;
        this.userService = userService;
        this.membershipService = membershipService;
    }

    // ====== MEETING MANAGEMENT ======
    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubId) or @userSecurity.isAdmin(authentication)")
    public Meeting createMeeting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Long clubId,
            MeetingCreateRequest meetingCreateRequest) {

        User user = userService.requireUserById(userDetails.getUser().getUserID());
        Club club = clubService.requireClubById(clubId);

        return meetingService.createMeeting(
                club,
                user,
                meetingCreateRequest.title(),
                meetingCreateRequest.startTime(),
                meetingCreateRequest.endTime(),
                meetingCreateRequest.link(),
                meetingCreateRequest.location()
        );
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public Meeting updateMeeting(Long clubID, Long MeetingID, MeetingUpdateRequest meetingUpdateRequest) {
        Meeting meeting = meetingService.requireById(MeetingID);

        meeting.setTitle(meetingUpdateRequest.title());
        meeting.setStartTime(meetingUpdateRequest.startTime());
        meeting.setEndTime(meetingUpdateRequest.endTime());
        meeting.setLink(meetingUpdateRequest.link());
        meeting.setLocation(meetingUpdateRequest.location());

        return meetingService.updateMeeting(meeting);
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void deleteMeeting(Long clubID, Long MeetingID) {
        meetingService.deleteMeeting(MeetingID);
    }

    // ====== CLUB MEMBER ======
    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public ClubMembership addMember(Long clubID, Long userID) {
        User user = userService.requireUserById(userID);
        Club club = clubService.requireClubById(clubID);
        return membershipService.enrollUserToClub(club, user);
    }

    // TODO what if the owner gets removed; set next oldest member as OWNER
    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void removeMember(Long clubID, Long userID) {
        User user = userService.requireUserById(userID);
        Club club = clubService.requireClubById(clubID);
        membershipService.deRegisterUserFromClub(user, club);
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void changeRole(Long clubID, Long userID, ClubRole role) {
        Club club = clubService.requireClubById(clubID);
        User user = userService.requireUserById(userID);

        membershipService.modifyClubRole(Set.of(role), user, club);
    }

    // ====== DISCUSSION ======

    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public DiscussionPrompt createDiscussion(Long clubID, String promptText, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Club club = clubService.requireClubById(clubID);
        User user = userDetails.getUser();
        return discussionPromptService.createPrompt(promptText, user, club);
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public void deletePrompt(Long clubID, Long promptID) {
        discussionPromptService.deleteByPromptID(promptID);
    }
}
