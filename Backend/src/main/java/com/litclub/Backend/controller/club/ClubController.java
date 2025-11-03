package com.litclub.Backend.controller.club;

import com.litclub.Backend.config.ConfigurationManager;
import com.litclub.Backend.construct.club.ClubActivityReport;
import com.litclub.Backend.construct.club.ClubCreateRequest;
import com.litclub.Backend.construct.club.ClubDashboard;
import com.litclub.Backend.construct.club.ClubStatistics;
import com.litclub.Backend.construct.meeting.MeetingCreateRequest;
import com.litclub.Backend.construct.meeting.MeetingUpdateRequest;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubMembership;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.MeetingService;
import com.litclub.Backend.service.middle.UserService;
import com.litclub.Backend.service.top.facilitator.ClubActivityService;
import com.litclub.Backend.service.top.gatekeeper.AdminService;
import com.litclub.Backend.service.top.gatekeeper.ClubModService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;
    private final AdminService adminService;
    private final UserService userService;
    private final ConfigurationManager configuration;
    private final ClubModService clubModService;
    private final ClubMembershipService clubMembershipService;
    private final ClubActivityService clubActivityService;
    private final MeetingService meetingService;

    public ClubController(ClubService clubService,
                          AdminService adminService,
                          ConfigurationManager configuration,
                          UserService userService,
                          ClubModService clubModService, ClubMembershipService clubMembershipService, ClubActivityService clubActivityService, MeetingService meetingService) {
        this.clubService = clubService;
        this.adminService = adminService;
        this.userService = userService;
        this.configuration = configuration;
        this.clubModService = clubModService;
        this.clubMembershipService = clubMembershipService;
        this.clubActivityService = clubActivityService;
        this.meetingService = meetingService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<Club>> getClubs(
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(adminService.getAllClubs(pageable));
    }

    @GetMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Club> getClub(
            @PathVariable("clubID") Long clubID
    ) {
        return ResponseEntity.ok(clubService.requireClubById(clubID));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Club> createClub(@Valid @RequestBody ClubCreateRequest clubRequest) {
        // TODO Club queues
        if (configuration
                .getInstanceSettings()
                .clubCreationMode()
                .equals(ConfigurationManager.ClubCreationMode.ADMIN_ONLY)
                &&
                SecurityContextHolder.getContext().getAuthentication()
                        .getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATOR"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User creator = userService.requireUserById(clubRequest.creator().userID());

        Club club = new Club();
        club.setClubName(clubRequest.clubName());
        club.setDescription(clubRequest.description());
        club.setCreator(creator);

        Club registeredClub = clubService.registerClub(club, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredClub);
    }

    @PutMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isOwner(authentication, clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Club> updateClub(
            @PathVariable("clubID") Long clubID,
            @Valid @RequestBody ClubCreateRequest clubRequest
    ) {
        Club club = clubService.requireClubById(clubID);
        return ResponseEntity.status(HttpStatus.OK).body(clubService.updateClub(clubRequest, club));
    }

    @DeleteMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isOwner(authentication, clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteClub(
            @PathVariable("clubID") Long clubID
    ) {
        clubService.deleteClub(clubID);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{clubID}/members")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<UserRecord>> getClubMembers(
            @PathVariable Long clubID,
            @PageableDefault Pageable pageable
    ) {
        List<User> members = clubService.getUsersForClub(clubID);
        Page<UserRecord> page = UserService.convertUserListToRecordPage(members, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{clubID}/members")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<ClubMembership> addUserToClub(
            @PathVariable Long clubID,
            @RequestBody UserRecord userRecord
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                clubModService.addMember(clubID, userRecord.userID())
        );
    }

    @DeleteMapping("/{clubID}/members/{userID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> removeUserFromClub(
            @PathVariable Long clubID,
            @PathVariable Long userID
    ) {
        clubModService.removeMember(clubID, userID);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clubID}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ClubMembership> joinClub(
            @PathVariable Long clubID,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        boolean isClubOpen = configuration.getClubFlags(clubID).enableRegister();
        User user = customUserDetails.getUser();
        Club club = clubService.requireClubById(clubID);
        if (!isClubOpen && !user.getGlobalRoles().contains(GlobalRole.ADMINISTRATOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clubMembershipService.enrollUserToClub(club, user));
    }

    @PostMapping("/{clubID}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leaveClub(
            @PathVariable Long clubID,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Club club = clubService.requireClubById(clubID);
        User user = customUserDetails.getUser();
        ClubMembership clubMembership = clubMembershipService.getMembershipByClubAndUser(club, user);
        clubMembershipService.deRegisterUserFromClub(clubMembership.getClubMembershipID());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/dashboard")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubDashboard> getClubDashboard(
            @PathVariable Long clubID
    ) {
        return ResponseEntity.ok(clubActivityService.getClubDashboard(clubID));
    }

    @GetMapping("/{clubID}/activity")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubActivityReport> getClubActivityReport(
            @PathVariable Long clubID
    ) {
        return ResponseEntity.ok(clubActivityService.getClubActivity(clubID));
    }

    @GetMapping("/{clubID}/activity/stats")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubStatistics> getClubActivityStats(
            @PathVariable Long clubID
    ) {
        return ResponseEntity.ok(clubActivityService.getClubStatistics(clubID));
    }


    @GetMapping("/{clubID}/meetings")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Page<Meeting>> getClubMeetings(
            @PathVariable Long clubID,
            @PageableDefault Pageable pageable
    ) {
        Club club = clubService.requireClubById(clubID);
        Page<Meeting> meetings = meetingService.getMeetingsForClub(club, pageable);
        return ResponseEntity.ok(meetings);
    }

    @PostMapping("/{clubID}/meetings")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Meeting> addMeeting(
            @PathVariable Long clubID,
            @RequestBody MeetingCreateRequest createRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clubModService.createMeeting(customUserDetails, clubID, createRequest));
    }

    @GetMapping("/{clubID}/meetings/{meetingID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Meeting> getMeeting(
            @PathVariable("clubID") Long clubID,
            @PathVariable("meetingID") Long meetingID
    ) {
        Meeting meeting = meetingService.requireById(meetingID);
        if (!meeting.getClub().getClubID().equals(clubID)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(meeting);
    }

    @PostMapping("/{clubID}/meetings/{meetingID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Meeting> updateMeeting(
            @PathVariable("clubID") Long clubID,
            @PathVariable("meetingID") Long meetingID,
            @RequestBody MeetingUpdateRequest meetingUpdateRequest
    ) {
        Meeting meeting = meetingService.requireById(meetingID);
        if (!meeting.getClub().getClubID().equals(clubID)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(clubModService.updateMeeting(
                clubID, meetingID, meetingUpdateRequest
        ));
    }

    @DeleteMapping("/{clubID}/meetings/{meetingID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable("clubID") Long clubID,
            @PathVariable("meetingID") Long meetingID
    ) {
        Meeting meeting = meetingService.requireById(meetingID);
        if (!meeting.getClub().getClubID().equals(clubID)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        clubModService.deleteMeeting(clubID, meetingID);
        return ResponseEntity.noContent().build();
    }
}
