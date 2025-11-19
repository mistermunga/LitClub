package com.litclub.ui.main.shared.view.service;

import com.litclub.client.api.ApiErrorHandler;
import com.litclub.construct.Review;
import com.litclub.construct.interfaces.library.ReviewRequest;
import com.litclub.persistence.repository.LibraryRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ReviewService {

    private final LibraryRepository libraryRepository;
    private final ObservableList<Review> reviewList = FXCollections.observableArrayList();

    public ReviewService() {
        this.libraryRepository = LibraryRepository.getInstance();
    }

    public void fetchReviews(
            Long bookID,
            Runnable onSuccess,
            Consumer<String> onError) {

        libraryRepository.fetchReviewsRecursive(bookID, 0, new ArrayList<>())
                .thenAccept(reviews -> {
                    Platform.runLater(() -> {
                        reviewList.setAll(reviews);
                        System.out.println("Reviews Loaded successfully");
                        onSuccess.run();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        String errorMessage = ApiErrorHandler.parseError(throwable);
                        System.err.println("Failed to fetch reviews: " + errorMessage);
                        onError.accept("Failed to fetch reviews: " + errorMessage);
                    });
                    return null;
                });
    }

    public void addReview(
            Long userID,
            Long bookID,
            ReviewRequest reviewRequest,
            Consumer<Review> onSuccess,
            Consumer<String> onError) {
        libraryRepository.createOrUpdateReview(userID, bookID, reviewRequest)
                .thenAccept(review -> {
                    Platform.runLater(() -> {
                        System.out.println("Review added successfully");
                        onSuccess.accept(review);
                    });
                }).exceptionally(throwable -> {
                    Platform.runLater(() -> {
                    String errorMessage = ApiErrorHandler.parseError(throwable);
                    System.err.println("Failed to add review: " + errorMessage);
                    onError.accept("Failed to add review: " + errorMessage);
                    });
                    return null;
                });
    }

    public ObservableList<Review> getReviewList() {
        return reviewList;
    }
}
