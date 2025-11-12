package com.litclub.Backend.controller.meeting;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.MeetingService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final ClubService clubService;
    private final UserService userService;

    public MeetingController(MeetingService meetingService, ClubService clubService, UserService userService) {
        this.meetingService = meetingService;
        this.clubService = clubService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<Meeting>> getAllMeetings(Pageable pageable) {
        return ResponseEntity.ok(meetingService.getAllMeetings(pageable));
    }

    @GetMapping("/{meetingID}/attendees")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<UserRecord>> getAttendees(@PathVariable Long meetingID) {
        Meeting meeting = meetingService.requireById(meetingID);
        List<User> users = meetingService.getAttendeesForMeeting(meeting);
        return ResponseEntity.ok(
                users.stream().map(UserService::convertUserToRecord).toList()
        );
    }

    @GetMapping("/club/{clubID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<Meeting>> getMeetingsByClubID(
            @PathVariable Long clubID,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                meetingService.getMeetings(clubService.requireClubById(clubID), pageable)
        );
    }

    @GetMapping("/user/{userID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Page<Meeting>> getMeetingsByUserID(
            @PathVariable Long userID,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                meetingService.getMeetings(
                        userService.requireUserById(userID),
                        pageable
                )
        );
    }
}
