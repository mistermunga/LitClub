package com.litclub.Backend.controller.meeting;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.User;
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

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
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
}
