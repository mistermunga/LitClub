package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.construct.book.BookDTO;
import com.litclub.Backend.construct.user.UserProfile;
import com.litclub.Backend.construct.user.UserActivityReport;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.low.ReviewService;
import com.litclub.Backend.service.middle.BookService;
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
    private final BookService bookService;

    public UserActivityService(
            UserService userService,
            MeetingService meetingService,
            ReviewService reviewService,
            NoteService noteService,
            DiscussionPromptService promptService,
            ClubService clubService,
            BookService bookService) {
        this.userService = userService;
        this.meetingService = meetingService;
        this.reviewService = reviewService;
        this.noteService = noteService;
        this.promptService = promptService;
        this.clubService = clubService;
        this.bookService = bookService;
    }

    // ====== USERDATA ======
    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public UserActivityReport getUserActivity(Long userID) {
        User user = userService.requireUserById(userID);

        List<Meeting> meetings = getMeetingsForUser(userID);
        List<Meeting> upcomingMeetings = meetings.stream()
                .filter(meeting -> meeting.getStartTime().isAfter(LocalDateTime.now()))
                .toList();
        List<Meeting> pastMeetings = meetings.stream()
                .filter(meeting -> meeting.getStartTime().isBefore(LocalDateTime.now()))
                .toList();

        List<Review> reviews = getReviewsForUser(userID);
        List<Note> notes = getNotesForUser(userID);
        List<DiscussionPrompt> prompts = getDiscussionPromptsFromUser(userID);

        return new UserActivityReport(
                UserService.convertUserToRecord(user),
                upcomingMeetings,
                pastMeetings,
                limitToRecent(reviews, Comparator.comparing(Review::getCreatedAt), 15),
                limitToRecent(notes, Comparator.comparing(Note::getCreatedAt), 20),
                prompts
        );
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public UserProfile getUserProfile(Long userID) {
        User user = userService.requireUserById(userID);

        List<BookDTO> bookDTOs = getBooksForUser(userID).stream()
                .map(book -> bookService.convertBookToDTO(book, user))
                .toList();

        List<Club> clubs = getClubsForUser(userID);
        List<Meeting> meetings = getMeetingsForUser(userID);
        List<Note> notes = getNotesForUser(userID);
        List<Review> reviews = getReviewsForUser(userID);

        return new UserProfile(
                UserService.convertUserToRecord(user),
                clubs,
                bookDTOs,
                meetings.size(),
                notes.size(),
                reviews.size()
        );
    }

    // ====== CROSSDOMAIN QUERIES ======
    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public List<Meeting> getMeetingsForUser(Long userID) {
        return meetingService.getMeetingsForUser(userService.requireUserById(userID));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public List<Review> getReviewsForUser(Long userID) {
        return reviewService.getReviews(userService.requireUserById(userID));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public List<Note> getNotesForUser(Long userID) {
        return noteService.getAllNotes(userService.requireUserById(userID));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public List<Club> getClubsForUser(Long userID) {
        return clubService.getClubsByUser(userService.requireUserById(userID));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public List<Book> getBooksForUser(Long userID) {
        return bookService.getBooksByUser(userService.requireUserById(userID));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public List<DiscussionPrompt> getDiscussionPromptsFromUser(Long userID) {
        return promptService.findAllByPoster(userService.requireUserById(userID));
    }

    // ====== UTILITY ======
    private static <T> List<T> limitToRecent(List<T> items, Comparator<T> comparator, int limit) {
        if (items.size() <= limit) return items;
        return items.stream()
                .sorted(comparator.reversed())
                .limit(limit)
                .toList();
    }
}
