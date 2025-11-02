package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.construct.library.book.BookStatus;
import com.litclub.Backend.construct.library.BookAddRequest;
import com.litclub.Backend.construct.library.BookWithStatus;
import com.litclub.Backend.construct.library.ReviewRequest;
import com.litclub.Backend.construct.library.UserLibrary;
import com.litclub.Backend.construct.review.ReviewDTO;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Review;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.UserBook;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.service.low.ReviewService;
import com.litclub.Backend.service.low.UserBooksService;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibraryManagementService {

    private final BookService bookService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final UserBooksService userBooksService;

    public LibraryManagementService(
            BookService bookService,
            UserService userService,
            ReviewService reviewService,
            UserBooksService ubs
    ) {
        this.bookService = bookService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.userBooksService = ubs;
    }

    // ====== LIBRARY MANAGEMENT ======
    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public UserLibrary getUserLibrary(Long userID){
        User user = userService.requireUserById(userID);
        List<Book> books = bookService.getBooksByUser(user);
        List<Review> reviews = reviewService.getReviews(user);

        return new UserLibrary(
                UserService.convertUserToRecord(user),
                books.stream()
                        .map(book -> convertBookToBookWithStatus(book, user))
                        .filter(bws -> bws.status().equals(BookStatus.READING))
                        .toList(),
                books.stream()
                        .map(book -> convertBookToBookWithStatus(book, user))
                        .filter(bws -> bws.status().equals(BookStatus.WANT_TO_READ))
                        .toList(),
                books.stream()
                        .map(book -> convertBookToBookWithStatus(book, user))
                        .filter(bws -> bws.status().equals(BookStatus.READ))
                        .toList(),
                books.stream()
                        .map(book -> convertBookToBookWithStatus(book, user))
                        .filter(bws -> bws.status().equals(BookStatus.DNF))
                        .toList(),
                reviews
        );
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public BookWithStatus addBookToLibrary(Long userID, BookAddRequest addRequest) {
        User user = userService.requireUserById(userID);
        Book book = bookService.createBook(addRequest);

        userBooksService.addUserBook(user, book, addRequest.initialStatus());
        return convertBookToBookWithStatus(book, user);
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public BookWithStatus updateBookStatus(Long userID, Long bookID, BookStatus bookStatus) {
        User user =  userService.requireUserById(userID);
        Book book = bookService.getBook(bookID);

        userBooksService.changeStatus(user, book, bookStatus);
        return convertBookToBookWithStatus(book, user);
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public Review rateAndReviewBook(Long userID, Long bookID, ReviewRequest reviewRequest) {
        User user = userService.requireUserById(userID);
        Book book = bookService.getBook(bookID);
        UserBook libraryItem = userBooksService.getUserBookByUserAndBook(user, book);

        ReviewDTO reviewDTO = new ReviewDTO(book, user, reviewRequest.rating(), reviewRequest.content());
        Review review = reviewService.createReview(reviewDTO);

        if (review.getRating() != null) userBooksService.changeRating(libraryItem, review.getRating());

        return review;
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public void deleteReview(Long userID, Long BookID) {
        User user = userService.requireUserById(userID);
        Review review = reviewService.getReviewByUserAndBook(
                user,
                bookService.getBook(BookID)
        );
        if (user.getGlobalRoles().contains(GlobalRole.ADMINISTRATOR) || review.getUser().equals(user)) {
            reviewService.deleteReview(review);
        }
    }

    // ------ UTILITY ------
    private BookWithStatus convertBookToBookWithStatus(Book book, User user){
        UserBook libraryItem = userBooksService.getUserBookByUserAndBook(user,book);
        return new BookWithStatus(
                book,
                libraryItem.getStatus(),
                libraryItem.getRating(),
                libraryItem.getDateStarted(),
                libraryItem.getDateFinished()
        );
    }
}
