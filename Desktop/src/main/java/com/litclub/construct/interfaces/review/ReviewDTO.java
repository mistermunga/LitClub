package com.litclub.construct.interfaces.review;

public class ReviewDTO {
    private Long bookID;
    private Long userID;
    private Integer rating;
    private String content;

    public Long getBookID() {
        return bookID;
    }

    public void setBookID(Long bookID) {
        this.bookID = bookID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
