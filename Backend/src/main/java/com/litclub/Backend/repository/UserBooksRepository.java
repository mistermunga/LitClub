package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.entity.UserBook;
import com.litclub.Backend.entity.compositeKey.UserBookID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserBooksRepository extends JpaRepository<UserBook, UserBookID> {

    Optional<UserBook> findUserBooksByUserAndBook(User user, Book book);
    Optional<UserBook> findUserBooksByUserBookID(UserBookID userBookID);

    List<UserBook> findAllByBook(Book book);
    List<UserBook> findAllByUser(User user);

    @Query("""
    SELECT ub.book
    FROM UserBook ub
    JOIN ClubMembership cm ON cm.member = ub.user
    WHERE cm.club IN (
        SELECT cm2.club
        FROM ClubMembership cm2
        WHERE cm2.member = :user
    )
    GROUP BY ub.book
    ORDER BY COUNT(ub.book) DESC
    """)
    Page<Book> findRecommendedBooks(User user, Pageable pageable);

}
