package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter @Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookID;

    @Column(name = "title", nullable = false)
    private String title;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "author_name")
    private List<String> authors = new ArrayList<>();

    @Column(name = "primary_author")
    private String primaryAuthor;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "year")
    private LocalDate year;

    @Column(name = "edition")
    private String edition;

    @ManyToOne
    @JoinColumn(name = "added_by")
    private User addedBy;

    // --- Convenience Methods ---

    /** Ensures primaryAuthor stays in sync with authors list. */
    public void setAuthors(List<String> authors) {
        this.authors = (authors != null) ? new ArrayList<>(authors) : new ArrayList<>();
        this.primaryAuthor = (this.authors.isEmpty()) ? "Unknown" : this.authors.getFirst();
    }

    /** Adds an author and updates primaryAuthor if needed. */
    public void addAuthor(String authorName) {
        if (authorName != null && !authorName.isBlank()) {
            if (this.authors == null) {
                this.authors = new ArrayList<>();
            }
            if (!this.authors.contains(authorName)) {
                this.authors.add(authorName);
            }
            if (this.primaryAuthor == null || this.primaryAuthor.equals("Unknown")) {
                this.primaryAuthor = this.authors.getFirst();
            }
        }
    }

    /** Always returns the first author if available, otherwise "Unknown". */
    public String getPrimaryAuthor() {
        if (authors != null && !authors.isEmpty()) {
            primaryAuthor = authors.getFirst();
            return authors.getFirst();
        }
        return "Unknown";
    }

    public String getAuthorsAsString() {
        return (authors != null && !authors.isEmpty())
                ? String.join(", ", authors)
                : "Unknown";
    }

    public void setPublishDate(String publishDate) {
        if (publishDate == null || publishDate.isBlank()) return;
        try {
            int yearValue = Integer.parseInt(publishDate.trim());
            this.year = LocalDate.of(yearValue, 1, 1);
        } catch (NumberFormatException e) {
            try {
                this.year = LocalDate.parse(publishDate);
            } catch (Exception ex) {
                java.util.regex.Matcher matcher =
                        java.util.regex.Pattern.compile("\\b(\\d{4})\\b").matcher(publishDate);
                if (matcher.find()) {
                    int year = Integer.parseInt(matcher.group(1));
                    this.year = LocalDate.of(year, 1, 1);
                }
            }
        }
    }

    public void setPublishers(String publishers) {
        this.publisher = publishers;
    }

    public void setAuthor(String primaryAuthor) {
        this.primaryAuthor = primaryAuthor;
        authors.addFirst(primaryAuthor);
    }
}
