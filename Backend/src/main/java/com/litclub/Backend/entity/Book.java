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

    /**
     * List of author names for this book.
     * Stored as an ElementCollection since authors are simple strings.
     * If you need full Author entities with more details, consider creating
     * a separate Author entity with a @ManyToMany relationship instead.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "author_name")
    private List<String> authors = new ArrayList<>();

    @Column
    private String primaryAuthor = authors.getFirst();

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

    @JoinColumn(name = "added_by")
    @ManyToOne
    private User addedBy;

    /**
     * Convenience method to get the primary author (first in the list).
     *
     * @return the primary author, or "Unknown" if no authors exist
     */
    public String getPrimaryAuthor() {
        if (authors != null && !authors.isEmpty()) {
            return authors.getFirst();
        }
        return "Unknown";
    }

    /**
     * Convenience method to get all authors as a comma-separated string.
     *
     * @return authors joined by ", " or "Unknown" if empty
     */
    public String getAuthorsAsString() {
        if (authors != null && !authors.isEmpty()) {
            return String.join(", ", authors);
        }
        return "Unknown";
    }

    /**
     * Adds an author to the list if not already present.
     *
     * @param primaryAuthor the author name to add
     */
    public void setAuthor(String primaryAuthor) {
        if (primaryAuthor != null && !primaryAuthor.isBlank()) {
            if (this.authors == null) {
                this.authors = new ArrayList<>();
            }
            if (!this.authors.contains(primaryAuthor)) {
                this.authors.add(primaryAuthor);
            }
        }
    }

    public void setPublishDate(String publishDate) {
        if (publishDate == null || publishDate.isBlank()) {
            return;
        }

        try {
            // Try parsing just the year (most common format from Open Library)
            int yearValue = Integer.parseInt(publishDate.trim());
            setYear(LocalDate.of(yearValue, 1, 1));
        } catch (NumberFormatException e) {
            // Try full date parsing
            try {
                LocalDate date = LocalDate.parse(publishDate);
                setYear(date);
            } catch (Exception ex) {
                // Fallback: extract year using regex
                java.util.regex.Pattern yearPattern = java.util.regex.Pattern.compile("\\b(\\d{4})\\b");
                java.util.regex.Matcher matcher = yearPattern.matcher(publishDate);
                if (matcher.find()) {
                    int year = Integer.parseInt(matcher.group(1));
                    setYear(LocalDate.of(year, 1, 1));
                }
                // If all else fails, just ignore it
            }
        }
    }

    public void setPublishers(String publishers) {
        this.publisher = publishers;
    }
}