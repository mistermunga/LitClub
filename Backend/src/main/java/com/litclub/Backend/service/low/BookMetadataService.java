package com.litclub.Backend.service.low;

import com.litclub.Backend.construct.book.clientDTO.BookMetadataDTO;
import com.litclub.Backend.service.OpenLibraryClient;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.repository.BookRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * <p>Service that enriches Book (JPA entity) using the OpenLibraryClient.</p>
 *
 * <p>Features:</p>
 * <ul>
 * <li>Client calls are rate-limited {@code Resilience4j RateLimiter} tuned for Open Library {@code safe default ~2 req/s}.</li>
 * <li>Simple retry on transient failures {@code Resilience4j Retry}.</li>
 * <li>In-memory caching of enriched Book entities {@code Caffeine} to avoid repeated remote calls.</li>
 *</ul>
 *
 * @see OpenLibraryClient
 *
 */
@Service
@Slf4j
public class BookMetadataService {

    private final OpenLibraryClient openLibraryClient;
    private final BookRepository bookRepository;

    // Small in-memory cache keyed by ISBN to reduce remote traffic. Adjust expiry/size as needed.
    private final Cache<String, Book> isbnCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(6))
            .maximumSize(10_000)
            .build();

    // Resilience4j RateLimiter: tuned conservatively below Open Library's 180r/m (≈3r/s).
    // This default is 2 requests/second (burst allowed up to 2 per refresh period).
    private final RateLimiter rateLimiter;

    // Retry config for transient errors
    private final Retry retry;

    public BookMetadataService(OpenLibraryClient openLibraryClient, BookRepository bookRepository) {
        this.openLibraryClient = openLibraryClient;
        this.bookRepository = bookRepository;

        RateLimiterConfig rlConfig = RateLimiterConfig.custom()
                // number of permits per refresh period
                .limitForPeriod(2)
                // refresh period
                .limitRefreshPeriod(Duration.ofSeconds(1))
                // max wait time when acquiring a permit
                .timeoutDuration(Duration.ofSeconds(3))
                .build();
        this.rateLimiter = RateLimiter.of("openlibrary-api", rlConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .retryExceptions(RuntimeException.class)
                .build();
        this.retry = Retry.of("openlibrary-retry", retryConfig);
    }

    /**
     * Enriches a Book from OpenLibrary using ISBN, persists it if found, and returns the saved entity.
     * If the book is already present in the cache or DB, returns the existing entity.
     */
    @Transactional
    public Optional<Book> enrichAndSaveByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) return Optional.empty();

        // Normalize isbn key
        String key = isbn.trim();

        // 1) Check cache
        Book cached = isbnCache.getIfPresent(key);
        if (cached != null) return Optional.of(cached);

        // 2) Check DB (to avoid hitting remote for already-imported books)
        Optional<Book> existing = bookRepository.findBookByisbn(key);
        if (existing.isPresent()) {
            isbnCache.put(key, existing.get());
            return existing;
        }

        // 3) Build a Supplier that calls the client
        Supplier<Optional<BookMetadataDTO>> remoteCall = () -> openLibraryClient.fetchByIsbn(key);

        // 4) Decorate with rate limiter and retry
        Supplier<Optional<BookMetadataDTO>> decorated = Decorators.ofSupplier(remoteCall)
                .withRateLimiter(rateLimiter)
                .withRetry(retry)
                .decorate();

        try {
            Optional<BookMetadataDTO> maybeDto = decorated.get();
            if (maybeDto.isEmpty()) return Optional.empty();

            BookMetadataDTO dto = maybeDto.get();
            Book book = mapDtoToEntity(dto, key);
            Book saved = bookRepository.save(book);

            isbnCache.put(key, saved);
            return Optional.of(saved);
        } catch (Exception ex) {
            log.warn("Failed to enrich/save ISBN {}: {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Search by title+author, take first result, and persist it.
     * Returns Optional.empty() if nothing found.
     */
    @Transactional
    public Optional<Book> enrichAndSaveByTitleAndAuthor(String title, String author) {
        if ((title == null || title.isBlank()) && (author == null || author.isBlank())) return Optional.empty();

        Supplier<Optional<BookMetadataDTO>> remoteCall = () -> openLibraryClient.fetchByTitleAndAuthor(title, author);
        Supplier<Optional<BookMetadataDTO>> decorated = Decorators.ofSupplier(remoteCall)
                .withRateLimiter(rateLimiter)
                .withRetry(retry)
                .decorate();

        try {
            Optional<BookMetadataDTO> maybe = decorated.get();
            if (maybe.isEmpty()) return Optional.empty();

            BookMetadataDTO dto = maybe.get();

            // In enrichAndSaveByTitleAndAuthor method:
            String isbnKey = dto.getIsbn();

            // If no ISBN, check DB by title+author first
            if (isbnKey == null) {
                // Try to find existing book by title and author
                Optional<Book> existingByTitle = bookRepository.findBookByTitleAndPrimaryAuthor(
                        dto.getTitle(),
                        dto.getPrimaryAuthor()
                );
                if (existingByTitle.isPresent()) {
                    isbnCache.put(dto.getTitle() + "|" + dto.getPrimaryAuthor(), existingByTitle.get());
                    return existingByTitle;
                }
                // Generate composite key for new book
                isbnKey = dto.getTitle() + "|" + dto.getPrimaryAuthor();
            }

            Book book = mapDtoToEntity(dto, isbnKey);
            Book saved = bookRepository.save(book);
            isbnCache.put(isbnKey, saved);
            return Optional.of(saved);
        } catch (Exception ex) {
            log.warn("Failed to search/enrich '{}' by '{}': {}", title, author, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Bulk search/save from a list of ISBNs. This method will respect the same rate limits.
     */
    @Transactional
    public List<Book> enrichAndSaveBulkByIsbns(List<String> isbns) {
        return isbns.stream()
                .distinct()
                .map(this::enrichAndSaveByIsbn)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // --- Mapping helper (simple) ---
    private Book mapDtoToEntity(BookMetadataDTO dto, String isbnKey) {
        Book book = new Book();
        book.setIsbn(isbnKey);
        book.setTitle(dto.getTitle());

        // Authors: use primary author helper if available else join list
        if (dto.hasAuthor()) {
            book.setAuthor(dto.getPrimaryAuthor());
        } else if (dto.getAuthors() != null && !dto.getAuthors().isEmpty()) {
            book.setAuthor(String.join(", ", dto.getAuthors()));
        }

        // Publish date and cover
        book.setPublishDate(dto.getPublishDate());
        String coverURL = dto.getCoverUrl();
        if (coverURL != null && !coverURL.isEmpty()) {book.setCoverUrl(coverURL);}

        // Publishers
        if (dto.getPublishers() != null) {
            book.setPublishers(String.join(", ", dto.getPublishers()));
        }

        // Edition / other identifiers
        if (dto.getEdition() != null) book.setEdition(dto.getEdition());

        return book;
    }

}

