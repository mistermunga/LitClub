package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter @Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookID;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "year")
    private LocalDate year;

    @Column(name = "edition")
    private String edition;

    @JoinColumn(name = "added_by")
    @ManyToOne
    private User addedBy;

}
