package com.litclub.Backend.construct.book.clientDTO;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data Transfer Object for book metadata fetched from Open Library API.
 * Acts as an intermediary between the API response and the Book entity.
 *
 * <p>This DTO contains only the fields we care about from Open Library,
 * making it easier to map to the domain model and avoiding tight coupling
 * with the external API structure.</p>
 */
@Data
@NoArgsConstructor
@Getter @Setter
public class BookMetadataDTO {

    /**
     * Book title.
     */
    private String title;

    /**
     * List of author names.
     * Multiple authors are common for academic books, edited volumes, etc.
     */
    private List<String> authors;

    /**
     * ISBN (can be ISBN-10 or ISBN-13).
     * This is the primary identifier for a specific edition.
     */
    private String isbn;

    /**
     * URL to the book's cover image.
     * Typically, from covers.openlibrary.org.
     */
    private String coverUrl;

    /**
     * Publisher name.
     */
    private String publisher;

    /**
     * Publish date as a string (format varies: "2005", "January 2005", etc.).
     */
    private String publishDate;

    /**
     * Edition identifier or description.
     * Can be used to distinguish between different editions of the same work.
     */
    private String edition;

    /**
     * Checks if this DTO has the minimum required data to create a Book entity.
     *
     * @return true if title and at least one author are present
     */
    public boolean isValid() {
        return title != null && !title.isBlank()
                && authors != null && !authors.isEmpty();
    }

    /**
     * Gets the primary author (first in the list).
     *
     * @return the first author, or "Unknown Author" if none exist
     */
    public String getPrimaryAuthor() {
        if (authors != null && !authors.isEmpty()) {
            return authors.getFirst();
        }
        return "Unknown Author";
    }

    public void setAuthor(String author) {
        if (author != null && !author.isBlank()) {
            if (this.authors == null) {
                this.authors = new ArrayList<>();
            }
            this.authors.add(author);
        }
    }

    public boolean hasAuthor() {
        return authors != null && !authors.isEmpty();
    }

    public List<String> getPublishers() {
        if (publisher == null || publisher.isBlank()) {
            return List.of();
        }
        return Arrays.asList(publisher.split("\\s*,\\s*"));
    }
}