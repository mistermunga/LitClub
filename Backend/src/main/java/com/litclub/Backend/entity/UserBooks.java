package com.litclub.Backend.entity;

import com.litclub.Backend.entity.compositeKey.UserBooksID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_books")
@Getter @Setter
public class UserBooks {

    @EmbeddedId
    private UserBooksID userBooksID;

    @MapsId("userID")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("bookID")
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(length = 30)
    private String status;

    @Column
    private Integer rating;

    @Column(name = "date_started")
    private LocalDate dateStarted;

    @Column(name = "date_finished")
    private LocalDate dateFinished;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
