package com.litclub.Backend.construct.book.clientDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTOs to deserialize Open Library API responses.
 *
 * <p>Included classes:</p>
 * <ul>
 *   <li>{@link OpenLibSearchResponse} — maps <code>/search.json</code> responses (top-level)</li>
 *   <li>{@link OpenLibDoc} — represents a single result inside the search response (<code>docs[]</code>)</li>
 * </ul>
 *
 * <p><strong>Note:</strong> All classes are annotated with
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} so the mapper will ignore unmapped fields.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class OpenLibSearchResponse {

    /** Total number of results found. */
    private int numFound;

    /** List of documents (individual search results). */
    private List<OpenLibDoc> docs;
}


