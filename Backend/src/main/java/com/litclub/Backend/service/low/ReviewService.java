package com.litclub.Backend.service.low;

import com.litclub.Backend.construct.review.ReviewDTO;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Review;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.repository.ReviewRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService (ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // ====== CREATE ======
    @Transactional
    public Review createReview(ReviewDTO reviewDTO) {
        if (reviewRepository.existsByUserAndBook(reviewDTO.getUser(), reviewDTO.getBook())) {
            throw new EntityExistsException("Review already exists");
        }
        Review review = convertDTOToReview(reviewDTO);
        return reviewRepository.save(review);
    }

    // ====== READ ======
    @Transactional(readOnly = true)
    public List<Review> getAll() {
        return reviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Review getReviewByUserAndBook(User user, Book book) {
        return reviewRepository.findByBookAndUser(book, user)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    @Transactional(readOnly = true)
    public List<Review> getReviews(User user) {
        return reviewRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Review getReview(Long reviewID) {
        return reviewRepository.findById(reviewID).orElseThrow(() -> new EntityNotFoundException("Review not found"));
    }

    @Transactional(readOnly = true)
    public List<Review> getReviews(Book book) {
        return reviewRepository.findByBook(book);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviews(Integer rating) {
        return reviewRepository.findByRatingEquals(rating);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(User user) {
        List<Review> reviews = getReviews(user);
        return reviews.stream()
                .map(Review::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Book book) {
        List<Review> reviews = getReviews(book);
        return reviews.stream()
                .map(Review::getRating)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }


    @Transactional(readOnly = true)
    public List<Review> getRatedReviewsForUser(User user, Integer rating) {
        return reviewRepository.findByUserAndRatingEquals(user, rating);
    }

    @Transactional(readOnly = true)
    public List<Review> getRatedReviewsForBook(Book book, Integer rating) {
        return reviewRepository.findByBookAndRatingEquals(book, rating);
    }

    @Transactional(readOnly = true)
    public List<Review> searchUserReviews(User user, String query) {
        return reviewRepository.findByUserAndContentContainsIgnoreCase(user, query);
    }

    @Transactional(readOnly = true)
    public List<Review> searchBookReviews(Book book, String query) {
        return reviewRepository.findByBookAndContentContainsIgnoreCase(book, query);
    }

    @Transactional(readOnly = true)
    public List<Review> generalSearch(String query) {
        return reviewRepository.findByContentContainsIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<Review> getBookReviewsRatedAbove(Book book, Integer rating) {
        return reviewRepository.findByBookAndRatingGreaterThan(book, rating);
    }

    @Transactional(readOnly = true)
    public List<Review> getUserReviewsRatedAbove(User user, Integer rating) {
        return reviewRepository.findByUserAndRatingGreaterThan(user, rating);
    }

    @Transactional(readOnly = true)
    public List<Review> getBookReviewsRatedBelow(Book book, Integer rating) {
        return reviewRepository.findByBookAndRatingLessThan(book, rating);
    }

    @Transactional(readOnly = true)
    public List<Review> getUserReviewsRatedBelow(User user, Integer rating) {
        return reviewRepository.findByUserAndRatingLessThan(user, rating);
    }

    // ====== UPDATE ======
    @Transactional
    public Review updateReview(ReviewDTO reviewDTO) {
        if (!reviewRepository.existsByUserAndBook(reviewDTO.getUser(), reviewDTO.getBook())) {
            throw new EntityNotFoundException("Review not found");
        }

        Review review = getReviewByUserAndBook(reviewDTO.getUser(), reviewDTO.getBook());
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        return reviewRepository.save(review);
    }

    // ====== DELETE ======
    @Transactional
    public void deleteReview(User user, Book book) {
        Review review = getReviewByUserAndBook(user, book);
        reviewRepository.delete(review);
    }

    @Transactional
    public void deleteReview(Review review) {
        reviewRepository.delete(review);
    }

    @Transactional
    public void purgeUserReviews(User user) {
        List<Review> reviews = getReviews(user);
        reviewRepository.deleteAll(reviews);
    }

    // ------ utility ------
    public Review convertDTOToReview(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setUser(reviewDTO.getUser());
        review.setBook(reviewDTO.getBook());
        review.setContent(reviewDTO.getContent());
        review.setRating(reviewDTO.getRating());

        return review;
    }
}
