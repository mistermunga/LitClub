package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Review;
import com.litclub.Backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndBook(User user, Book book);

    Optional<Review> findByBookAndUser(Book book, User user);

    List<Review> findByUser(User user);
    Page<Review> findByUser(User user, Pageable pageable);
    List<Review> findByBook(Book book);
    Page<Review> findByBook(Book book, Pageable pageable);
    List<Review> findByRatingEquals(int rating);

    List<Review> findByUserAndRatingEquals(User user, Integer rating);
    List<Review> findByBookAndRatingEquals(Book book, Integer rating);
    @Query("""
    SELECT r FROM Review r
    WHERE r.book = :book
      AND LOWER(CAST(r.content AS string)) LIKE LOWER(CONCAT('%', :content, '%'))
""")
    List<Review> findByBookAndContentContainsIgnoreCase(@Param("book") Book book, @Param("content") String content);

    @Query("""
    SELECT r FROM Review r
    WHERE r.user = :user
      AND LOWER(CAST(r.content AS string)) LIKE LOWER(CONCAT('%', :content, '%'))
""")
    List<Review> findByUserAndContentContainsIgnoreCase(@Param("user") User user, @Param("content") String content);

    @Query("""
    SELECT r FROM Review r
    WHERE LOWER(CAST(r.content AS string)) LIKE LOWER(CONCAT('%', :content, '%'))
""")
    List<Review> findByContentContainsIgnoreCase(@Param("content") String content);

    List<Review> findByBookAndRatingGreaterThan(Book book, Integer rating);
    List<Review> findByUserAndRatingGreaterThan(User user, Integer rating);
    List<Review> findByBookAndRatingLessThan(Book book, Integer rating);
    List<Review> findByUserAndRatingLessThan(User user, Integer rating);

    boolean existsByReviewID(Long reviewID);
}
