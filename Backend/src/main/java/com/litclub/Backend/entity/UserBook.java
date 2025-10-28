package com.litclub.Backend.entity;

import com.litclub.Backend.construct.book.BookStatus;
import com.litclub.Backend.entity.compositeKey.UserBookID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_books")
@Getter @Setter
public class UserBook {

    @EmbeddedId
    private UserBookID userBookID;

    @MapsId("userID")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @MapsId("bookID")
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    /**
     * Specifies {@link User}'s relationship with a {@link Book}.
     * Value defined by an {@link BookStatus} enum
     */
    @Column(length = 30)
    private BookStatus status;

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
