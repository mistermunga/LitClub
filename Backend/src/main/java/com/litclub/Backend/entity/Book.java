package com.litclub.Backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a published or catalogued {@code Book} within the LitClub ecosystem.
 *
 * <p>This entity captures bibliographic metadata such as title, authorship,
 * publication details, and identifiers. Books form the foundation of a user's
 * personal library and are central to many core features of LitClub, including
 * collections, recommendations, and club reading lists.</p>
 *
 * <p><strong>Key Relationships:</strong></p>
 * <ul>
 *   <li><strong>{@link #addedBy} (Many-to-One):</strong> References the {@link User}
 *       who added this book to the system. A user may contribute multiple books,
 *       but each book record is attributed to exactly one uploader.</li>
 * </ul>
 *
 * <p><strong>Core Attributes:</strong></p>
 * <ul>
 *   <li><strong>{@link #bookID}:</strong> Primary key identifier.</li>
 *   <li><strong>{@link #title}:</strong> The book’s title, required for all records.</li>
 *   <li><strong>{@link #authors}:</strong> A list of all authors associated with the book.</li>
 *   <li><strong>{@link #primaryAuthor}:</strong> The first listed author, synchronized automatically with {@link #authors}.</li>
 *   <li><strong>{@link #isbn}:</strong> A unique ISBN, used for external metadata retrieval.</li>
 * </ul>
 *
 * <p><strong>Lifecycle Notes:</strong></p>
 * <ul>
 *   <li>When authors are set via {@link #setAuthors(List)}, the {@link #primaryAuthor} is automatically
 *       updated to the first element of the list (or defaults to {@code "Unknown"} if empty).</li>
 *   <li>Convenience methods such as {@link #addAuthor(String)} and {@link #getAuthorsAsString()}
 *       maintain or expose a consistent author representation.</li>
 *   <li>The {@link #setPublishDate(String)} method provides lenient date parsing, accommodating formats like
 *       {@code "2001"} or {@code "September 2001"} for flexible metadata ingestion.</li>
 * </ul>
 *
 * <p><strong>Integration Notes:</strong></p>
 * <ul>
 *   <li>Metadata enrichment is handled by {@link com.litclub.Backend.service.low.BookMetadataService},
 *       which may populate missing information based on the title, author, or ISBN.</li>
 *   <li>Domain-level operations such as creation, retrieval, and curation are managed by
 *       {@link com.litclub.Backend.service.middle.BookService}.</li>
 * </ul>
 *
 * @see User
 * @see com.litclub.Backend.service.low.BookMetadataService
 * @see com.litclub.Backend.service.middle.BookService
 */
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

    /** Returns {@link Book#authors} as a string*/
    public String getAuthorsAsString() {
        return (authors != null && !authors.isEmpty())
                ? String.join(", ", authors)
                : "Unknown";
    }

    /** <p>Parses the Year from a String value. Formatted to recognise two formats
     * {@code "2001"} or {@code "September 2001"}. Sets the value of {@link Book#authors}
     * </p>
     *
     * @param publishDate the date as a string, fetched by {@link com.litclub.Backend.service.low.BookMetadataService}
     */
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
