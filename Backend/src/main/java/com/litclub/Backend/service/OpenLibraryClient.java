package com.litclub.Backend.service;

import com.litclub.Backend.construct.book.clientDTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Client for querying the Open Library API to fetch book metadata.
 *
 * <p>Supports two search strategies:</p>
 * <ul>
 *   <li>Search by ISBN (uses search endpoint for reliability)</li>
 *   <li>Search by title and author combination</li>
 * </ul>
 *
 * <p><strong>Note:</strong> ISBN lookups now use the search API instead of the ISBN endpoint
 * because the ISBN endpoint has poor coverage. The search API is more reliable for finding
 * books by their ISBN.</p>
 *
 * <p><strong>API Documentation:</strong>
 * <a href="https://openlibrary.org/developers/api">Open Library API</a></p>
 */
@Service
@Slf4j
public class OpenLibraryClient {

    private static final String BASE_URL = "https://openlibrary.org";
    private static final String SEARCH_ENDPOINT = "/search.json";

    private static final String USER_AGENT = "LitClub/1.0 (rynjeru@usiu.ac.ke)";

    private final RestClient restClient;

    public OpenLibraryClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
    }

    /**
     * Fetches book metadata by ISBN using the search API.
     *
     * <p>This method uses the search endpoint instead of the ISBN endpoint because
     * the ISBN endpoint has very poor coverage. Many valid ISBNs that exist in
     * Open Library's database are only accessible via search.</p>
     *
     * @param isbn the ISBN-10 or ISBN-13 (with or without hyphens)
     * @return Optional containing BookMetadataDTO if found, empty otherwise
     */
    public Optional<BookMetadataDTO> fetchByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            log.warn("ISBN is null or blank");
            return Optional.empty();
        }

        // Clean the ISBN (remove hyphens and spaces)
        String cleanIsbn = isbn.replaceAll("[\\s-]", "");

        try {
            log.info("Fetching book metadata for ISBN: {}", cleanIsbn);

            // Use search API with ISBN parameter - much more reliable than /isbn/{isbn}.json
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(SEARCH_ENDPOINT)
                    .queryParam("isbn", cleanIsbn)
                    .queryParam("fields", "key,title,author_name,isbn,first_publish_year," +
                            "publish_year,publisher,cover_i,publish_date,edition_key")
                    .queryParam("limit", 1); // We only need the top result

            OpenLibSearchResponse response = restClient.get()
                    .uri(uriBuilder.build().toUriString())
                    .retrieve()
                    .body(OpenLibSearchResponse.class);

            if (response != null && response.getDocs() != null && !response.getDocs().isEmpty()) {
                OpenLibDoc topResult = response.getDocs().getFirst();
                BookMetadataDTO dto = mapSearchDocToDTO(topResult);
                // Ensure the ISBN we searched for is set (in case the doc has multiple ISBNs)
                if (dto.getIsbn() == null || dto.getIsbn().isEmpty()) {
                    dto.setIsbn(cleanIsbn);
                }
                log.info("Found book for ISBN {}: {}", cleanIsbn, dto.getTitle());
                return Optional.of(dto);
            }

            log.warn("No data found for ISBN: {}", cleanIsbn);
            return Optional.empty();

        } catch (RestClientException e) {
            log.error("Error fetching book by ISBN {}: {}", cleanIsbn, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Searches for book metadata by title and author using the Search API.
     * Returns the best matching result based on Open Library's relevance ranking.
     *
     * @param title the book title (required)
     * @param author the author name (optional, but improves accuracy)
     * @return Optional containing BookMetadataDTO of the best match, empty if not found
     */
    public Optional<BookMetadataDTO> fetchByTitleAndAuthor(String title, String author) {
        if (title == null || title.isBlank()) {
            log.warn("Title is null or blank");
            return Optional.empty();
        }

        try {
            log.info("Searching for book: title='{}', author='{}'", title, author);

            // Build the search query with specific fields for better performance
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(SEARCH_ENDPOINT)
                    .queryParam("title", title)
                    .queryParam("fields", "key,title,author_name,isbn,first_publish_year," +
                            "publish_year,publisher,cover_i,publish_date,edition_key")
                    .queryParam("limit", 1); // We only need the top result

            if (author != null && !author.isBlank()) {
                uriBuilder.queryParam("author", author);
            }

            OpenLibSearchResponse response = restClient.get()
                    .uri(uriBuilder.build().toUriString())
                    .retrieve()
                    .body(OpenLibSearchResponse.class);

            if (response != null && response.getDocs() != null && !response.getDocs().isEmpty()) {
                OpenLibDoc topResult = response.getDocs().getFirst();
                return Optional.of(mapSearchDocToDTO(topResult));
            }

            log.warn("No results found for title='{}', author='{}'", title, author);
            return Optional.empty();

        } catch (RestClientException e) {
            log.error("Error searching for book by title and author: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Searches for multiple book results by title and author.
     * Useful when you want to present multiple options to the user.
     *
     * @param title the book title (required)
     * @param author the author name (optional)
     * @param limit maximum number of results to return (default: 5)
     * @return List of BookMetadataDTO objects, empty list if no results
     */
    public List<BookMetadataDTO> searchBooks(String title, String author, int limit) {
        if (title == null || title.isBlank()) {
            log.warn("Title is null or blank");
            return List.of();
        }

        try {
            log.info("Searching for books: title='{}', author='{}', limit={}", title, author, limit);

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(SEARCH_ENDPOINT)
                    .queryParam("title", title)
                    .queryParam("fields", "key,title,author_name,isbn,first_publish_year," +
                            "publish_year,publisher,cover_i,publish_date,edition_key")
                    .queryParam("limit", Math.min(limit, 20)); // Cap at 20 to avoid excessive data

            if (author != null && !author.isBlank()) {
                uriBuilder.queryParam("author", author);
            }

            OpenLibSearchResponse response = restClient.get()
                    .uri(uriBuilder.build().toUriString())
                    .retrieve()
                    .body(OpenLibSearchResponse.class);

            if (response != null && response.getDocs() != null) {
                return response.getDocs().stream()
                        .map(this::mapSearchDocToDTO)
                        .toList();
            }

            return List.of();

        } catch (RestClientException e) {
            log.error("Error searching for books: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Maps a search document to BookMetadataDTO.
     */
    private BookMetadataDTO mapSearchDocToDTO(OpenLibDoc doc) {
        BookMetadataDTO dto = new BookMetadataDTO();
        dto.setTitle(doc.getTitle());

        // Authors
        if (doc.getAuthorName() != null && !doc.getAuthorName().isEmpty()) {
            dto.setAuthors(doc.getAuthorName());
        } else {
            dto.setAuthors(List.of("Unknown Author"));
        }

        // ISBN (prefer the first one)
        if (doc.getIsbn() != null && !doc.getIsbn().isEmpty()) {
            dto.setIsbn(doc.getIsbn().getFirst());
        }

        // Publisher (take the first one)
        if (doc.getPublisher() != null && !doc.getPublisher().isEmpty()) {
            dto.setPublisher(doc.getPublisher().getFirst());
        }

        // Publish date
        doc.getLatestPublishYear().ifPresent(year -> dto.setPublishDate(String.valueOf(year)));

        // Cover URL
        doc.getCoverUrl().ifPresent(dto::setCoverUrl);

        // Edition key (for reference)
        if (doc.getEditionKey() != null && !doc.getEditionKey().isEmpty()) {
            dto.setEdition(doc.getEditionKey().getFirst());
        }

        return dto;
    }
}