package com.litclub.Backend.construct.book.clientDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * Represents a single document (book entry) returned in a search response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class OpenLibDoc {

    /** Title of the work. */
    private String title;

    /** Author names (array of strings). */
    @JsonProperty("author_name")
    private List<String> authorName;

    /** List of ISBNs associated with the work. */
    @JsonProperty("isbn")
    private List<String> isbn;

    /** First year the work was published. */
    @JsonProperty("first_publish_year")
    private Integer firstPublishYear;

    /** All known publication years (may contain multiple values). */
    @JsonProperty("publish_year")
    private List<Integer> publishYear;

    /** List of publishers. */
    @JsonProperty("publisher")
    private List<String> publisher;

    /** Cover ID — used to build cover URLs via covers.openlibrary.org. */
    @JsonProperty("cover_i")
    private Integer coverId;

    /** Human-readable publish dates (e.g. "January 1, 2005"). */
    @JsonProperty("publish_date")
    private List<String> publishDate;

    /** Edition keys associated with this document. */
    @JsonProperty("edition_key")
    private List<String> editionKey;

    /**
     * Attempts to find the most recent (latest) publish year, if available.
     *
     * @return an {@link Optional} containing the latest publish year, or empty if none found.
     */
    public Optional<Integer> getLatestPublishYear() {
        if (publishYear != null && !publishYear.isEmpty()) {
            return publishYear.stream().max(Integer::compareTo);
        }
        return Optional.ofNullable(firstPublishYear);
    }

    /**
     * Builds a cover image URL using the cover ID, if present.
     *
     * @return an {@link Optional} containing the cover URL, or empty if unavailable.
     */
    public Optional<String> getCoverUrl() {
        if (coverId != null) {
            return Optional.of("https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg");
        }
        return Optional.empty();
    }
}
