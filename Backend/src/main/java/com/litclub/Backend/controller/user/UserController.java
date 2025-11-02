package com.litclub.Backend.controller.user;

import com.litclub.Backend.construct.book.BookStatus;
import com.litclub.Backend.construct.library.BookAddRequest;
import com.litclub.Backend.construct.library.BookWithStatus;
import com.litclub.Backend.construct.library.ReviewRequest;
import com.litclub.Backend.construct.library.UserLibrary;
import com.litclub.Backend.construct.note.NoteCreateRequest;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.construct.user.*;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.UserService;
import com.litclub.Backend.service.top.facilitator.DiscussionManagementService;
import com.litclub.Backend.service.top.facilitator.LibraryManagementService;
import com.litclub.Backend.service.top.facilitator.UserActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserActivityService userActivityService;
    private final LibraryManagementService libraryManagementService;
    private final BookService bookService;
    private final DiscussionManagementService discussionManagementService;

    public UserController(UserService userService,
                          UserActivityService userActivityService,
                          LibraryManagementService libraryManagementService,
                          BookService bookService,
                          DiscussionManagementService discussionManagementService) {
        this.userService = userService;
        this.userActivityService = userActivityService;
        this.libraryManagementService = libraryManagementService;
        this.bookService = bookService;
        this.discussionManagementService = discussionManagementService;
    }

    // ====== USER INFO ======

    @GetMapping("/me")
    public ResponseEntity<UserRecord> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        User user = userDetails.getUser();
        return ResponseEntity.ok(UserService.convertUserToRecord(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserRecord> updateCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserRegistrationRecord userRegistrationRecord
    ){
        User user = userDetails.getUser();
        if (!user.getUsername().equals(userRegistrationRecord.username())) {
            return ResponseEntity.badRequest().build();
        }
        UserRecord ur = userService.updateUser(user.getUserID(), userRegistrationRecord);
        return ResponseEntity.ok(ur);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Boolean> deleteCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.deleteUser(userDetails.getUserID());
        return ResponseEntity.ok(true);
    }


    // ====== USER ACTIVITY ======

    @GetMapping("/{userID}/profile")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable("userID") Long userID){
        UserProfile up = userActivityService.getUserProfile(userID);
        return ResponseEntity.ok(up);
    }

    @GetMapping("/{userID}/activity")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<UserActivityReport> getUserActivityReport(@PathVariable("userID") Long userID){
        UserActivityReport uar = userActivityService.getUserActivity(userID);
        return ResponseEntity.ok(uar);
    }

    @GetMapping("/{userID}/statistics")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<UserStatistics> getUserStatistics(@PathVariable("userID") Long userID){
        UserStatistics us = userActivityService.getUserStatistics(userID);
        return ResponseEntity.ok(us);
    }


    @GetMapping("/{userID}/clubs")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<List<Club>> getClubs(@PathVariable("userID") Long userID){
        return ResponseEntity.ok(
                userActivityService.getClubsForUser(userID)
        );
    }


    // ====== USER LIBRARY ======

    @GetMapping("/{userID}/library")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<UserLibrary> getUserLibrary(@PathVariable("userID") Long userID) {
        return ResponseEntity.ok(libraryManagementService.getUserLibrary(userID));
    }

    @PostMapping("/{userID}/library")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public BookWithStatus addBookToLibrary(
            @PathVariable("userID") Long userID,
            @RequestBody BookAddRequest bookAddRequest
    ) {
        return libraryManagementService.addBookToLibrary(userID, bookAddRequest);
    }

    @PutMapping("/{userID}/library/{bookID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<BookWithStatus> updateBookStatus(
            @PathVariable("userID") Long userID,
            @PathVariable("bookID") Long bookID,
            @RequestBody BookStatus bookStatus
    ) {
        return ResponseEntity.ok(libraryManagementService.updateBookStatus(userID, bookID, bookStatus));
    }

    @DeleteMapping("/{userID}/library/{bookID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Boolean> deleteBookFromLibrary(
            @PathVariable("userID") Long userID,
            @PathVariable("bookID") Long bookID
    ) {
        userService.removeBookFromLibrary(userID, bookService.getBook(bookID));
        return ResponseEntity.ok(true);
    }


    // ====== REVIEWS ======

    @GetMapping("/{userID}/reviews")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable("userID") Long userID) {
        return ResponseEntity.ok(userActivityService.getReviewsForUser(userID));
    }

    @PostMapping("/{userID}/reviews")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Review> addReview(
            @PathVariable("userID") Long userID,
            @RequestParam Long bookID,
            @RequestBody ReviewRequest reviewRequest
    ) {
        return ResponseEntity.ok(libraryManagementService.rateAndReviewBook(userID, bookID, reviewRequest));
    }

    @PutMapping("/{userID}/reviews/{bookID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Review> updateReview(
            @PathVariable("userID") Long userID,
            @PathVariable("bookID") Long bookID,
            @RequestBody ReviewRequest reviewRequest
    ) {
        return ResponseEntity.ok(libraryManagementService.rateAndReviewBook(userID, bookID, reviewRequest));
    }

    @PutMapping("/{userID}/reviews/{bookID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Boolean> deleteReview(
            @PathVariable("userID") Long userID,
            @PathVariable("bookID") Long bookID
    ){
        libraryManagementService.deleteReview(userID, bookID);
        return ResponseEntity.ok(true);
    }


    // ====== NOTES ======

    @GetMapping("{userID}/notes")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<List<Note>> getUserNotes(@PathVariable("userID") Long userID) {
        return ResponseEntity.ok(userActivityService.getNotesForUser(userID));
    }

    @PostMapping("{userID}/notes")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Note> addNote(
            @PathVariable("userID") Long userID,
            @RequestBody NoteCreateRequest createRequest
            ){
        return ResponseEntity.ok(discussionManagementService.createPrivateNote(userID, createRequest));
    }

    @PutMapping("{userID}/notes/{noteID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Note> updateNote(
            @PathVariable("userID") Long userID,
            @PathVariable("noteID") Long noteID,
            @RequestBody String content
    ) {
        return ResponseEntity.ok(discussionManagementService.updateNote(userID, noteID, content));
    }

    @DeleteMapping("{userID}/notes/{noteID}")
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public ResponseEntity<Boolean> deleteNote(
            @PathVariable("userID") Long userID,
            @PathVariable("noteID") Long noteID
    ) {
        discussionManagementService.deleteNote(userID, noteID);
        return ResponseEntity.ok(true);
    }
}