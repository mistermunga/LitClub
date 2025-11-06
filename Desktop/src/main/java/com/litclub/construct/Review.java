package com.litclub.construct;

import java.time.LocalDateTime;

@Deprecated
public class Review {

    private int reviewID;
    private int bookID;
    private int userID;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    public Review(int reviewID, int bookID, int userID, int rating, String content) {
        this.reviewID = reviewID;
        this.bookID = bookID;
        this.userID = userID;
        this.rating = rating;
        this.content = content;
    }

    public int getReviewID() {
        return reviewID;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper method to convert rating to star display (for UI components)
    public static String getStarRating(int rating) {
        double stars = rating / 2.0;
        int fullStars = (int) stars;
        boolean hasHalfStar = (stars - fullStars) >= 0.5;

        StringBuilder display = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            display.append("★");
        }
        if (hasHalfStar) {
            display.append("⯨");
        }
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        for (int i = 0; i < emptyStars; i++) {
            display.append("☆");
        }

        return display.toString() + " (" + stars + ")";
    }
}
