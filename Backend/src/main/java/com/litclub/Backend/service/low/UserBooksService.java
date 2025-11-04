package com.litclub.Backend.service.low;

import com.litclub.Backend.construct.library.book.BookStatus;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.UserBook;
import com.litclub.Backend.entity.compositeKey.UserBookID;
import com.litclub.Backend.exception.MissingLibraryItemException;
import com.litclub.Backend.repository.UserBooksRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserBooksService {

    private final UserBooksRepository userBooksRepository;

    public UserBooksService(UserBooksRepository userBooksRepository) {
        this.userBooksRepository = userBooksRepository;
    }

    // ====== CREATE ======
    @Transactional
    public UserBook addUserBook(User user, Book book, BookStatus status) {
        return userBooksRepository.findUserBooksByUserAndBook(user, book)
                .orElseGet(() -> {
                    UserBook newUserBook = new UserBook();
                    newUserBook.setUser(user);
                    newUserBook.setBook(book);
                    newUserBook.setStatus(status);

                    LocalDate today = LocalDate.now();

                    switch (status) {
                        case READING -> newUserBook.setDateStarted(today);
                        case WANT_TO_READ -> { /* no dates yet */ }
                        case READ -> newUserBook.setDateFinished(today);
                        case DNF -> newUserBook.setDateFinished(today);
                    }

                    return userBooksRepository.save(newUserBook);
                });
    }


    // ====== READ ======
    @Transactional(readOnly = true)
    public List<UserBook> getAllUserBooks() {
        return userBooksRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserBook getUserBookByUserAndBook(User user, Book book) {
        Optional<UserBook> userBook = userBooksRepository.findUserBooksByUserAndBook(user, book);
        return userBook.orElseThrow(
                () -> new MissingLibraryItemException(user.getUsername(), book.getTitle())
        );
    }

    @Transactional(readOnly = true)
    public UserBook getUserBookByUserAndBook(UserBookID userBookID) {
        Optional<UserBook> userBook = userBooksRepository.findUserBooksByUserBookID(userBookID);
        return userBook.orElseThrow(
                () -> new MissingLibraryItemException(userBookID.getUserID().toString(), userBookID.getBookID().toString())
        );
    }

    @Transactional(readOnly = true)
    public List<UserBook> getUserBooksForBook(Book book) {
        return userBooksRepository.findAllByBook(book);
    }

    @Transactional(readOnly = true)
    public List<UserBook> getUserBooksForUser(User user) {
        return userBooksRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersForBook(Book book) {
        List<UserBook> userBooks = getUserBooksForBook(book);
        List<User> readers = new ArrayList<>();
        for (UserBook userBook : userBooks) {
            readers.add(userBook.getUser());
        }
        return readers;
    }

    @Transactional(readOnly = true)
    public List<Book> getBooksForUser(User user) {
        List<UserBook> userBooks = getUserBooksForUser(user);
        List<Book> books = new ArrayList<>();
        for (UserBook userBook : userBooks) {
            books.add(userBook.getBook());
        }
        return books;
    }

    // ====== UPDATE ======
    @Transactional
    public UserBook updateUserBook(UserBook userBook) {

        UserBook newUserBook = getUserBookByUserAndBook(userBook.getUserBookID());
        newUserBook.setStatus(userBook.getStatus());
        newUserBook.setRating(userBook.getRating());
        newUserBook.setDateStarted(userBook.getDateStarted());
        newUserBook.setDateFinished(userBook.getDateFinished());

        return userBooksRepository.save(newUserBook);
    }

    @Transactional
    public UserBook changeStatus(User user, Book book, BookStatus status) {

        UserBook userBook = getUserBookByUserAndBook(user, book);
        if (status.equals(userBook.getStatus())) {return userBook;}

        if (status.equals(BookStatus.READ) || status.equals(BookStatus.DNF)) {
            userBook.setDateFinished(LocalDate.now());
        }

        userBook.setStatus(status);
        return userBooksRepository.save(userBook);
    }

    @Transactional
    public UserBook changeRating(UserBook userBook, Integer newRating) {
        userBook.setRating(newRating);
        return userBooksRepository.save(userBook);
    }

    // ====== DELETE ======
    @Transactional
    public void removeUserBook(User user, Book book) {
        UserBook userBook = getUserBookByUserAndBook(user, book);
        userBooksRepository.delete(userBook);
    }
}
