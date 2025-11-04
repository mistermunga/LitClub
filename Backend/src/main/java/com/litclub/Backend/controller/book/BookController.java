package com.litclub.Backend.controller.book;

import com.litclub.Backend.construct.library.BookAddRequest;
import com.litclub.Backend.construct.library.ReviewRequest;
import com.litclub.Backend.construct.library.book.BookSearchRequest;
import com.litclub.Backend.construct.review.ReviewDTO;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Review;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.ReviewService;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.UserService;
import com.litclub.Backend.service.top.facilitator.LibraryManagementService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;
    private final LibraryManagementService libraryManagementService;

    public BookController(
            BookService bookService,
            ReviewService reviewService,
            LibraryManagementService libraryManagementService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
        this.libraryManagementService = libraryManagementService;
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
    public ResponseEntity<Book> getBook(@PathVariable("bookID") Long bookID) {
        return ResponseEntity.ok(bookService.getBook(bookID));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Book>> searchBook(
            @RequestBody BookSearchRequest bsr
    ) {
        return ResponseEntity.ok(bookService.searchBook(bsr));
    }

    @GetMapping("/isbn/{isbn}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Book> getBookByISBN(@PathVariable("isbn") String isbn) {
        return ResponseEntity.ok(bookService.getBookByISBN(isbn));
    }

    // ====== Reviews ======
    @GetMapping("/{bookID}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Review>> getBookReviews(@PathVariable("bookID") Long bookID, Pageable pageable) {
        return ResponseEntity.ok(reviewService.getReviews(bookService.getBook(bookID), pageable));
    }

    @PostMapping("/{bookID}/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Review> postReview(
            @PathVariable("bookID") Long bookID,
            @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal CustomUserDetails cud
            ) {
        return ResponseEntity.ok(
                libraryManagementService.rateAndReviewBook(cud.getUserID(), bookID, reviewRequest)
        );
    }

    @PutMapping("/{bookID}/reviews/{reviewID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long bookID,
            @PathVariable Long reviewID,
            @RequestBody ReviewDTO review,
            @AuthenticationPrincipal CustomUserDetails cud
    ) {
        if (!bookID.equals(review.getBookID())) {
            throw new MalformedDTOException("bookID mismatch");
        }
        if (!cud.getUserID().equals(review.getUserID())) {
            throw new MalformedDTOException("userID mismatch");
        }
        return ResponseEntity.ok(
                reviewService.updateReview(review, reviewID, cud.getUser(), bookService.getBook(bookID))
        );
    }

    @DeleteMapping("/{bookID}/reviews/{reviewID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long bookID,
            @PathVariable Long reviewID
    ) {
        Review review = reviewService.getReview(reviewID);
        if (!review.getBook().getBookID().equals(bookID)) {
            throw new MalformedDTOException("bookID mismatch");
        }
        reviewService.deleteReview(review);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookID}/reviews/average")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Double> getBookAverage(@PathVariable("bookID") Long bookID) {
        return ResponseEntity.ok(reviewService.getAverageRating(bookService.getBook(bookID)));
    }

    @GetMapping("/{bookID}/readers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<UserRecord>> getReaders(
            Pageable pageable,
            @PathVariable("bookID") Long bookID
    ) {
        List<User> users = bookService.getUsersForBook(Map.of("bookID", bookID));
        Page<UserRecord> page = UserService.convertUserListToRecordPage(users, pageable);

        return ResponseEntity.ok(page);
    }

}