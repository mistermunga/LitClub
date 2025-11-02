package com.litclub.Backend.controller.book;

import com.litclub.Backend.construct.library.BookAddRequest;
import com.litclub.Backend.construct.library.book.BookSearchRequest;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Review;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.service.low.ReviewService;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final ReviewService reviewService;

    public BookController(
            BookService bookService,
            ReviewService reviewService
    ) {
        this.bookService = bookService;
        this.reviewService = reviewService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Book>> getBooks(Pageable pageable) {
        return ResponseEntity.ok(bookService.getBooks(pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Book> addBook(@RequestBody BookAddRequest bookAddRequest) {
        return ResponseEntity.ok(bookService.createBook(bookAddRequest));
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
    public ResponseEntity<List<Review>> getBookReviews(@PathVariable("bookID") Long bookID) {
        return ResponseEntity.ok(reviewService.getReviews(bookService.getBook(bookID)));
    }

    @GetMapping("/{bookID}/reviews/average")
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<Double> getBookAverage(@PathVariable("bookID") Long bookID) {
        return ResponseEntity.ok(reviewService.getAverageRating(bookService.getBook(bookID)));
    }

    @GetMapping("/{bookID}/readers")
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<Page<UserRecord>> getReaders(
            Pageable pageable,
            @PathVariable("bookID") Long bookID
    ) {
        List<User> users = bookService.getUsersForBook(Map.of("bookID", bookID));
        List<UserRecord> userRecords = users.stream()
                .map(UserService::convertUserToRecord)
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), userRecords.size());
        Page<UserRecord> page = new PageImpl<>(
                userRecords.subList(start, end),
                pageable,
                userRecords.size()
        );

        return ResponseEntity.ok(page);
    }

}
