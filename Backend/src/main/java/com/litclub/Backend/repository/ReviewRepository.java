package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Review;
import com.litclub.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndBook(User user, Book book);

    Optional<Review> findByBookAndUser(Book book, User user);

    List<Review> findByUser(User user);
    List<Review> findByBook(Book book);
    List<Review> findByRatingEquals(int rating);

    List<Review> findByUserAndRatingEquals(User user, int rating);
    List<Review> findByBookAndRatingEquals(Book book, int rating);
    List<Review> findByBookAndContentContainsIgnoreCase(Book book, String content);
    List<Review> findByUserAndContentContainsIgnoreCase(User user, String content);
    List<Review> findByContentContainsIgnoreCase(String content);
    List<Review> findByBookAndRatingGreaterThan(Book book, int rating);
    List<Review> findByUserAndRatingGreaterThan(User user, int rating);
    List<Review> findByBookAndRatingLessThan(Book book, int rating);
    List<Review> findByUserAndRatingLessThan(User user, int rating);

}
