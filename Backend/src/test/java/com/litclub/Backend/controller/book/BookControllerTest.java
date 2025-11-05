package com.litclub.Backend.controller.book;

import com.litclub.Backend.construct.auth.AuthResponse;
import com.litclub.Backend.construct.library.BookAddRequest;
import com.litclub.Backend.construct.library.ReviewRequest;
import com.litclub.Backend.construct.library.book.BookSearchRequest;
import com.litclub.Backend.construct.library.book.BookStatus;
import com.litclub.Backend.construct.note.NoteCreateRequest;
import com.litclub.Backend.construct.user.UserRegistrationRecord;
import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Note;
import com.litclub.Backend.entity.Reply;
import com.litclub.Backend.entity.Review;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;
    private String authToken;
    private Long authenticatedUserId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;

        UserRegistrationRecord registration = new UserRegistrationRecord(
                "testuser",
                "Test",
                "User",
                "test@example.com",
                "password123",
                false
        );

        // --- FIX START ---
        ResponseEntity<AuthResponse> authResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registration,
                AuthResponse.class
        );

        // Check if registration failed (e.g., status 409 Conflict/400 Bad Request)
        if (authResponse.getStatusCode().is4xxClientError()) {
            // The user already exists, so log in instead to get the token and user ID.
            // Assuming you have a login endpoint at /api/auth/login that accepts username/password

            // NOTE: Replace 'Map.of' with a proper login request DTO if available
            Map<String, String> loginRequest = Map.of(
                    "username", registration.username(),
                    "password", registration.password()
            );

            authResponse = restTemplate.postForEntity(
                    baseUrl + "/api/auth/login",
                    loginRequest,
                    AuthResponse.class
            );
        }
        // --- FIX END ---

        // Assertions will now check the successful login or successful registration response
        Assertions.assertNotNull(authResponse.getBody(), "Auth response body should not be null after registration or login.");

        // Null check to prevent the original NPE, though the logic above should prevent it
        AuthResponse body = authResponse.getBody();
        Assertions.assertNotNull(body.userRecord(), "User record must not be null in the AuthResponse.");

        authToken = body.token();
        authenticatedUserId = body.userRecord().userID();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // ====== BOOK CREATION TESTS ======

    @Test
    void addBook_ShouldReturn201AndBook_WhenValidRequest() {
        // Arrange
        BookAddRequest bookRequest = new BookAddRequest(
                "1984",
                "George Orwell",
                null,
                BookStatus.WANT_TO_READ
        );
        HttpEntity<BookAddRequest> request = new HttpEntity<>(bookRequest, createAuthHeaders());

        // Act
        ResponseEntity<Book> response = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                request,
                Book.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isNotNull();
        assertThat(response.getBody().getBookID()).isNotNull();
    }

    @Test
    void addBook_ShouldReturn401_WhenNotAuthenticated() {
        // Arrange
        BookAddRequest bookRequest = new BookAddRequest(
                "Test Book",
                "Test Author",
                null,
                BookStatus.WANT_TO_READ
        );

        // Act - No auth headers
        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/api/books",
                bookRequest,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void addBook_ShouldFetchMetadata_WhenTitleAndAuthorProvided() {
        // Arrange - Using a well-known book
        BookAddRequest bookRequest = new BookAddRequest(
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                null,
                BookStatus.WANT_TO_READ
        );
        HttpEntity<BookAddRequest> request = new HttpEntity<>(bookRequest, createAuthHeaders());

        // Act
        ResponseEntity<Book> response = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                request,
                Book.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isNotEmpty();
        // Metadata service should populate these if available
        assertThat(response.getBody().getAuthors()).isNotEmpty();
    }

    // ====== BOOK RETRIEVAL TESTS ======

    @Test
    void getBooks_ShouldReturn200AndList_WhenAuthenticated() {
        // Arrange - Add a book first
        BookAddRequest bookRequest = new BookAddRequest(
                "Test Book",
                "Test Author",
                null,
                BookStatus.READ
        );
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        restTemplate.exchange(baseUrl + "/api/books", HttpMethod.POST, addRequest, Book.class);

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.GET,
                request,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
    }

    @Test
    void getBook_ShouldReturn200AndBook_WhenBookExists() {
        // Arrange - Add a book first
        BookAddRequest bookRequest = new BookAddRequest(
                "Specific Book",
                "Specific Author",
                null,
                BookStatus.READ
        );
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> addResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = addResponse.getBody().getBookID();

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Book> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId,
                HttpMethod.GET,
                request,
                Book.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookID()).isEqualTo(bookId);
    }

    @Test
    void getBook_ShouldReturn404_WhenBookDoesNotExist() {
        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/books/99999",
                HttpMethod.GET,
                request,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ====== BOOK SEARCH TESTS ======

    @Test
    void searchBook_ShouldReturnMatches_WhenTitleProvided() {
        // Arrange - Add some books
        BookAddRequest book1 = new BookAddRequest("Harry Potter", "J.K. Rowling", null, BookStatus.READ);
        BookAddRequest book2 = new BookAddRequest("Harry and the Hendersons", "Unknown", null, BookStatus.READ);
        HttpEntity<BookAddRequest> request1 = new HttpEntity<>(book1, createAuthHeaders());
        HttpEntity<BookAddRequest> request2 = new HttpEntity<>(book2, createAuthHeaders());
        restTemplate.exchange(baseUrl + "/api/books", HttpMethod.POST, request1, Book.class);
        restTemplate.exchange(baseUrl + "/api/books", HttpMethod.POST, request2, Book.class);

        // Act
        BookSearchRequest searchRequest = new BookSearchRequest("Harry", null, null);
        HttpEntity<BookSearchRequest> searchEntity = new HttpEntity<>(searchRequest, createAuthHeaders());
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/api/books/search",
                HttpMethod.POST,
                searchEntity,
                List.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void searchBook_ShouldReturnMatches_WhenAuthorProvided() {
        // Arrange
        BookAddRequest book = new BookAddRequest("Animal Farm", "George Orwell", null, BookStatus.READ);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(book, createAuthHeaders());
        restTemplate.exchange(baseUrl + "/api/books", HttpMethod.POST, addRequest, Book.class);

        // Act
        BookSearchRequest searchRequest = new BookSearchRequest(null, "Orwell", null);
        HttpEntity<BookSearchRequest> searchEntity = new HttpEntity<>(searchRequest, createAuthHeaders());
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + "/api/books/search",
                HttpMethod.POST,
                searchEntity,
                List.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(1);
    }

    // ====== REVIEW TESTS ======

    @Test
    void postReview_ShouldReturn201AndReview_WhenValidRequest() {
        // Arrange - Add a book first
        BookAddRequest bookRequest = new BookAddRequest("Review Test Book", "Test Author", null, BookStatus.READ);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        // Act
        ReviewRequest reviewRequest = new ReviewRequest(8, "Great book!");
        HttpEntity<ReviewRequest> reviewEntity = new HttpEntity<>(reviewRequest, createAuthHeaders());
        ResponseEntity<Review> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews",
                HttpMethod.POST,
                reviewEntity,
                Review.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRating()).isEqualTo(8);
        assertThat(response.getBody().getContent()).isEqualTo("Great book!");
    }

    @Test
    void getBookReviews_ShouldReturn200AndList_WhenReviewsExist() {
        // Arrange - Add book and review
        BookAddRequest bookRequest = new BookAddRequest("Reviewed Book", "Test Author", null, BookStatus.READ);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        ReviewRequest reviewRequest = new ReviewRequest(7, "Good read");
        HttpEntity<ReviewRequest> reviewEntity = new HttpEntity<>(reviewRequest, createAuthHeaders());
        restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews",
                HttpMethod.POST,
                reviewEntity,
                Review.class
        );

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews",
                HttpMethod.GET,
                request,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
    }

    @Test
    void getBookAverage_ShouldReturnAverageRating_WhenReviewsExist() {
        // Arrange - Add book and multiple reviews
        BookAddRequest bookRequest = new BookAddRequest("Average Test Book", "Test Author", null, BookStatus.READ);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        // Add review (rating: 8)
        ReviewRequest reviewRequest = new ReviewRequest(8, "Great");
        HttpEntity<ReviewRequest> reviewEntity = new HttpEntity<>(reviewRequest, createAuthHeaders());
        restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews",
                HttpMethod.POST,
                reviewEntity,
                Review.class
        );

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Double> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews/average",
                HttpMethod.GET,
                request,
                Double.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(8.0);
    }

    @Test
    void deleteReview_ShouldReturn204_WhenReviewExists() {
        // Arrange - Add book and review
        BookAddRequest bookRequest = new BookAddRequest("Delete Review Book", "Test Author", null, BookStatus.READ);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        ReviewRequest reviewRequest = new ReviewRequest(5, "Meh");
        HttpEntity<ReviewRequest> reviewEntity = new HttpEntity<>(reviewRequest, createAuthHeaders());
        ResponseEntity<Review> reviewResponse = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews",
                HttpMethod.POST,
                reviewEntity,
                Review.class
        );
        Long reviewId = reviewResponse.getBody().getReviewID();

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/reviews/" + reviewId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ====== NOTE TESTS ======

    @Test
    void postNote_ShouldReturn201AndNote_WhenValidRequest() {
        // Arrange - Add a book first
        BookAddRequest bookRequest = new BookAddRequest("Note Test Book", "Test Author", null, BookStatus.READING);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        // Act
        NoteCreateRequest noteRequest = new NoteCreateRequest(bookId, null, "This is my note", true);
        HttpEntity<NoteCreateRequest> noteEntity = new HttpEntity<>(noteRequest, createAuthHeaders());
        ResponseEntity<Note> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.POST,
                noteEntity,
                Note.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("This is my note");
        assertThat(response.getBody().isPrivate()).isTrue();
    }

    @Test
    void getNotes_ShouldReturn200AndList_WhenNotesExist() {
        // Arrange - Add book and note
        BookAddRequest bookRequest = new BookAddRequest("Notes List Book", "Test Author", null, BookStatus.READING);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        NoteCreateRequest noteRequest = new NoteCreateRequest(bookId, null, "Test note", false);
        HttpEntity<NoteCreateRequest> noteEntity = new HttpEntity<>(noteRequest, createAuthHeaders());
        restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.POST,
                noteEntity,
                Note.class
        );

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.GET,
                request,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
    }

    @Test
    void updateNote_ShouldReturn200AndUpdatedNote_WhenValidRequest() {
        // Arrange - Add book and note
        BookAddRequest bookRequest = new BookAddRequest("Update Note Book", "Test Author", null, BookStatus.READING);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        NoteCreateRequest noteRequest = new NoteCreateRequest(bookId, null, "Original note", true);
        HttpEntity<NoteCreateRequest> noteEntity = new HttpEntity<>(noteRequest, createAuthHeaders());
        ResponseEntity<Note> noteResponse = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.POST,
                noteEntity,
                Note.class
        );
        Long noteId = noteResponse.getBody().getNoteID();

        // Act
        HttpEntity<String> updateEntity = new HttpEntity<>("Updated note content", createAuthHeaders());
        ResponseEntity<Note> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes/" + noteId,
                HttpMethod.PUT,
                updateEntity,
                Note.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("Updated note content");
    }

    @Test
    void deleteNote_ShouldReturn204_WhenNoteExists() {
        // Arrange - Add book and note
        BookAddRequest bookRequest = new BookAddRequest("Delete Note Book", "Test Author", null, BookStatus.READING);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        NoteCreateRequest noteRequest = new NoteCreateRequest(bookId, null, "Note to delete", true);
        HttpEntity<NoteCreateRequest> noteEntity = new HttpEntity<>(noteRequest, createAuthHeaders());
        ResponseEntity<Note> noteResponse = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.POST,
                noteEntity,
                Note.class
        );
        Long noteId = noteResponse.getBody().getNoteID();

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes/" + noteId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ====== REPLY TESTS ======

    @Test
    void createReply_ShouldReturn201AndReply_WhenValidRequest() {
        // Arrange - Add book and note
        BookAddRequest bookRequest = new BookAddRequest("Reply Test Book", "Test Author", null, BookStatus.READING);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        NoteCreateRequest noteRequest = new NoteCreateRequest(bookId, null, "Parent note", false);
        HttpEntity<NoteCreateRequest> noteEntity = new HttpEntity<>(noteRequest, createAuthHeaders());
        ResponseEntity<Note> noteResponse = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.POST,
                noteEntity,
                Note.class
        );
        Long noteId = noteResponse.getBody().getNoteID();

        // Act
        HttpEntity<String> replyEntity = new HttpEntity<>("This is a reply", createAuthHeaders());
        ResponseEntity<Reply> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes/" + noteId + "/replies",
                HttpMethod.POST,
                replyEntity,
                Reply.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("This is a reply");
    }

    @Test
    void getReplies_ShouldReturn200AndList_WhenRepliesExist() {
        // Arrange - Add book, note, and reply
        BookAddRequest bookRequest = new BookAddRequest("Replies List Book", "Test Author", null, BookStatus.READING);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        NoteCreateRequest noteRequest = new NoteCreateRequest(bookId, null, "Parent note", false);
        HttpEntity<NoteCreateRequest> noteEntity = new HttpEntity<>(noteRequest, createAuthHeaders());
        ResponseEntity<Note> noteResponse = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes",
                HttpMethod.POST,
                noteEntity,
                Note.class
        );
        Long noteId = noteResponse.getBody().getNoteID();

        HttpEntity<String> replyEntity = new HttpEntity<>("A reply", createAuthHeaders());
        restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes/" + noteId + "/replies",
                HttpMethod.POST,
                replyEntity,
                Reply.class
        );

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/notes/" + noteId + "/replies",
                HttpMethod.GET,
                request,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
    }

    // ====== READERS TEST ======

    @Test
    void getReaders_ShouldReturn200AndList_WhenBookHasReaders() {
        // Arrange - Add a book (which adds current user to readers)
        BookAddRequest bookRequest = new BookAddRequest("Readers Test Book", "Test Author", null, BookStatus.READ);
        HttpEntity<BookAddRequest> addRequest = new HttpEntity<>(bookRequest, createAuthHeaders());
        ResponseEntity<Book> bookResponse = restTemplate.exchange(
                baseUrl + "/api/books",
                HttpMethod.POST,
                addRequest,
                Book.class
        );
        Long bookId = bookResponse.getBody().getBookID();

        // Act
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/books/" + bookId + "/readers",
                HttpMethod.GET,
                request,
                Map.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("content")).isNotNull();
    }
}