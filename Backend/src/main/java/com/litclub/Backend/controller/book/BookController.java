package com.litclub.Backend.controller.book;

import com.litclub.Backend.construct.library.BookAddRequest;
import com.litclub.Backend.construct.library.ReviewRequest;
import com.litclub.Backend.construct.library.book.BookSearchRequest;
import com.litclub.Backend.construct.note.NoteCreateRequest;
import com.litclub.Backend.construct.review.ReviewDTO;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.low.ReviewService;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.ReplyService;
import com.litclub.Backend.service.middle.UserService;
import com.litclub.Backend.service.top.facilitator.LibraryManagementService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final LibraryManagementService libraryManagementService;
    private final NoteService noteService;
    private final ReplyService replyService;

    public BookController(BookService bookService, ReviewService reviewService,
                          LibraryManagementService libraryManagementService,
                          NoteService noteService, ReplyService replyService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
        this.libraryManagementService = libraryManagementService;
        this.noteService = noteService;
        this.replyService = replyService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Book>> getBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooks(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Book> addBook(@RequestBody BookAddRequest bookAddRequest) {
        Book book = bookService.createBook(bookAddRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping("/{bookID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Book> getBook(@PathVariable Long bookID) {
        return ResponseEntity.ok(bookService.getBook(bookID));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Book>> searchBook(@RequestBody BookSearchRequest bsr) {
        return ResponseEntity.ok(bookService.searchBook(bsr));
    }

    @GetMapping("/isbn/{isbn}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Book> getBookByISBN(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.getBookByISBN(isbn));
    }

    @GetMapping("/{bookID}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Review>> getBookReviews(@PathVariable Long bookID, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviews(bookService.getBook(bookID), pageable));
    }

    @PostMapping("/{bookID}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Review> postReview(@PathVariable Long bookID, @RequestBody ReviewRequest reviewRequest,
                                             @AuthenticationPrincipal CustomUserDetails cud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(libraryManagementService.rateAndReviewBook(cud.getUserID(), bookID, reviewRequest));
    }

    @PutMapping("/{bookID}/reviews/{reviewID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Review> updateReview(@PathVariable Long bookID, @PathVariable Long reviewID,
                                               @RequestBody ReviewDTO review,
                                               @AuthenticationPrincipal CustomUserDetails cud) {
        if (!bookID.equals(review.getBookID())) {
            throw new MalformedDTOException("bookID mismatch");
        }
        if (!cud.getUserID().equals(review.getUserID())) {
            throw new MalformedDTOException("userID mismatch");
        }
        return ResponseEntity.ok(reviewService.updateReview(review, reviewID, cud.getUser(), bookService.getBook(bookID)));
    }

    @DeleteMapping("/{bookID}/reviews/{reviewID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long bookID, @PathVariable Long reviewID) {
        Review review = reviewService.getReview(reviewID);
        validateReviewBelongsToBook(review, bookID);
        reviewService.deleteReview(review);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookID}/reviews/average")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Double> getBookAverage(@PathVariable Long bookID) {
        return ResponseEntity.ok(reviewService.getAverageRating(bookService.getBook(bookID)));
    }

    @GetMapping("/{bookID}/readers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<UserRecord>> getReaders(Pageable pageable, @PathVariable Long bookID) {
        List<User> users = bookService.getUsersForBook(Map.of("bookID", bookID));
        Page<UserRecord> page = UserService.convertUserListToRecordPage(users, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{bookID}/notes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Note>> getNotes(Pageable pageable, @PathVariable Long bookID) {
        Book book = bookService.getBook(bookID);
        return ResponseEntity.ok(noteService.getAllNotes(book, pageable));
    }

    @PostMapping("/{bookID}/notes")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Note> postNote(@PathVariable Long bookID, @RequestBody NoteCreateRequest ncr,
                                         @AuthenticationPrincipal CustomUserDetails cud) {
        if (!bookID.equals(ncr.bookID())) {
            throw new MalformedDTOException("bookID mismatch");
        }
        Book book = bookService.getBook(bookID);
        Note note = noteService.save(cud.getUser(), book, ncr.content(), Optional.empty(), Optional.empty(), ncr.isPrivate());
        return ResponseEntity.status(HttpStatus.CREATED).body(note);
    }

    @GetMapping("/{bookID}/notes/{noteID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Note> getNote(@PathVariable Long bookID, @PathVariable Long noteID,
                                        @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        validateNoteAccess(note, cud);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{bookID}/notes/{noteID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Note> updateNote(@PathVariable Long bookID, @PathVariable Long noteID,
                                           @RequestBody String content,
                                           @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        validateNoteOwnership(note, cud);
        return ResponseEntity.ok(noteService.updateNote(content, noteID));
    }

    @DeleteMapping("/{bookID}/notes/{noteID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteNote(@PathVariable Long bookID, @PathVariable Long noteID,
                                           @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        validateNoteOwnershipOrAdmin(note, cud);
        noteService.deleteNote(noteID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookID}/notes/{noteID}/replies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Reply>> getReplies(@PathVariable Long bookID, @PathVariable Long noteID,
                                                  @AuthenticationPrincipal CustomUserDetails cud, Pageable pageable) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        validateNoteAccess(note, cud);
        return ResponseEntity.ok(replyService.getRepliesForNote(note, pageable));
    }

    @PostMapping("/{bookID}/notes/{noteID}/replies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Reply> createReply(@PathVariable Long bookID, @PathVariable Long noteID,
                                             @RequestBody String content,
                                             @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        validateNoteAccess(note, cud);
        Reply reply = replyService.createReply(cud.getUser(), note, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    @PutMapping("/{bookID}/notes/{noteID}/replies/{replyID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Reply> updateReply(@PathVariable Long bookID, @PathVariable Long noteID,
                                             @PathVariable Long replyID, @RequestBody String content,
                                             @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        Reply reply = replyService.getReplyById(replyID);
        validateReplyBelongsToNote(reply, note);
        validateReplyOwnership(reply, cud);
        return ResponseEntity.ok(replyService.updateReplyContent(replyID, content));
    }

    @DeleteMapping("/{bookID}/notes/{noteID}/replies/{replyID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReply(@PathVariable Long bookID, @PathVariable Long noteID,
                                            @PathVariable Long replyID,
                                            @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = validateNoteBelongsToBook(bookID, noteID);
        Reply reply = replyService.getReplyById(replyID);
        validateReplyBelongsToNote(reply, note);
        validateReplyOwnershipOrAdmin(reply, cud);
        replyService.deleteReply(replyID);
        return ResponseEntity.noContent().build();
    }

    private void validateReviewBelongsToBook(Review review, Long bookID) {
        if (!review.getBook().getBookID().equals(bookID)) {
            throw new MalformedDTOException("review does not belong to book");
        }
    }

    private Note validateNoteBelongsToBook(Long bookID, Long noteID) {
        Note note = noteService.getNoteById(noteID);
        if (!note.getBook().getBookID().equals(bookID)) {
            throw new MalformedDTOException("note does not belong to book");
        }
        return note;
    }

    private void validateNoteAccess(Note note, CustomUserDetails cud) {
        if (note.isPrivate() && !note.getUser().equals(cud.getUser()) &&
                !cud.getAuthorities().contains(GlobalRole.ADMINISTRATOR)) {
            throw new AccessDeniedException("cannot access private note");
        }
    }

    private void validateNoteOwnership(Note note, CustomUserDetails cud) {
        if (!note.getUser().equals(cud.getUser())) {
            throw new MalformedDTOException("note does not belong to user");
        }
    }

    private void validateNoteOwnershipOrAdmin(Note note, CustomUserDetails cud) {
        if (!note.getUser().equals(cud.getUser()) && !cud.getAuthorities().contains(GlobalRole.ADMINISTRATOR)) {
            throw new AccessDeniedException("cannot delete another user's note");
        }
    }

    private void validateReplyBelongsToNote(Reply reply, Note note) {
        if (!reply.getParentNote().equals(note)) {
            throw new MalformedDTOException("reply does not belong to note");
        }
    }

    private void validateReplyOwnership(Reply reply, CustomUserDetails cud) {
        if (!reply.getUser().equals(cud.getUser())) {
            throw new MalformedDTOException("reply does not belong to user");
        }
    }

    private void validateReplyOwnershipOrAdmin(Reply reply, CustomUserDetails cud) {
        if (!reply.getUser().equals(cud.getUser()) && !cud.getAuthorities().contains(GlobalRole.ADMINISTRATOR)) {
            throw new AccessDeniedException("cannot delete another user's reply");
        }
    }
}