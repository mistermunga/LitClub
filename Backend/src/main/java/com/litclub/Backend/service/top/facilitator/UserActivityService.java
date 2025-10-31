package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.entity.*;
import com.litclub.Backend.construct.user.UserActivityReport;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.low.ReviewService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.MeetingService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class UserActivityService {

    private final UserService userService;
    private final MeetingService meetingService;
    private final ReviewService reviewService;
    private final NoteService noteService;
    private final DiscussionPromptService promptService;
    private final ClubService clubService;
    private final ClubMembershipService clubMembershipService;

    public UserActivityService(
            UserService userService,
            MeetingService meetingService,
            ReviewService reviewService,
            NoteService noteService,
            DiscussionPromptService promptService,
            ClubService clubService,
            ClubMembershipService clubMembershipService) {
        this.userService = userService;
        this.meetingService = meetingService;
        this.reviewService = reviewService;
        this.noteService = noteService;
        this.promptService = promptService;
        this.clubService = clubService;
        this.clubMembershipService = clubMembershipService;
    }

    // ====== STATISTICS ======
    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public UserActivityReport getUserActivity(Long userID) {
        User user = userService.requireUserById(userID);
        List<Meeting> meetings = meetingService.getMeetingsForUser(user);

        List<Meeting> upcomingMeetings = meetings.stream()
                .filter(meeting -> meeting.getStartTime().isAfter(LocalDateTime.now()))
                .toList();

        List<Meeting> pastMeetings = meetings
                .stream()
                .filter(meeting -> meeting.getStartTime().isBefore(LocalDateTime.now()))
                .toList();

        List<Review> reviews = reviewService.getReviews(user);
        List<Note> notes = noteService.getAllNotes(user);
        List<DiscussionPrompt> prompts = promptService.findAllByPoster(user);

        return new UserActivityReport(
                UserService.convertUserToRecord(user),
                upcomingMeetings,
                pastMeetings,
                (long) reviews.size() <= 15 ? reviews :
                        reviews.stream().sorted(Comparator.comparing(Review::getCreatedAt).reversed()).limit(15)
                                .toList(),
                (long) notes.size() <= 15 ? notes :
                        notes.stream().sorted(Comparator.comparing(Note::getCreatedAt).reversed()).limit(15)
                                .toList(),
                prompts
        );
    }
}
