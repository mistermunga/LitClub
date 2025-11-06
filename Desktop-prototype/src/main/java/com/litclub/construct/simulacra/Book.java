package com.litclub.construct.simulacra;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {

    private Long bookID;
    private String title;
    private List<String> authors = new ArrayList<>();
    private String primaryAuthor;
    private String isbn;
    private String coverUrl;
    private String publisher;
    private LocalDate year;
    private String edition;
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
     * @param publishDate the date as a string
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

    public Long getBookID() {
        return bookID;
    }

    public void setBookID(Long bookID) {
        this.bookID = bookID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setPrimaryAuthor(String primaryAuthor) {
        this.primaryAuthor = primaryAuthor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getYear() {
        return year;
    }

    public void setYear(LocalDate year) {
        this.year = year;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public User getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(User addedBy) {
        this.addedBy = addedBy;
    }
}
