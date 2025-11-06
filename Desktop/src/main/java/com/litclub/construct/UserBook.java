package com.litclub.construct;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.litclub.construct.compositeKey.UserBookID;
import com.litclub.construct.enums.BookStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserBook {
    private UserBookID userBookID;
    private User user;
    private Book book;
    private BookStatus status;
    private Integer rating;
    private LocalDate dateStarted;
    private LocalDate dateFinished;
    private LocalDateTime createdAt;

    public UserBookID getUserBookID() {
        return userBookID;
    }

    public void setUserBookID(UserBookID userBookID) {
        this.userBookID = userBookID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public LocalDate getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(LocalDate dateStarted) {
        this.dateStarted = dateStarted;
    }

    public LocalDate getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(LocalDate dateFinished) {
        this.dateFinished = dateFinished;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

