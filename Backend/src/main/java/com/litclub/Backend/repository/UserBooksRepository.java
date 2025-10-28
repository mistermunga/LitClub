package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.UserBook;
import com.litclub.Backend.entity.compositeKey.UserBookID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBooksRepository extends JpaRepository<UserBook, UserBookID> {

    Optional<UserBook> findUserBooksByUserAndBook(User user, Book book);
    Optional<UserBook> findUserBooksByUserBookID(UserBookID userBookID);

    List<UserBook> findAllByBook(Book book);
    List<UserBook> findAllByUser(User user);
}
