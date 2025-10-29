package com.litclub.Backend.service.middle;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.exception.BookNotFoundException;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.repository.BookRepository;
import com.litclub.Backend.service.low.BookMetadataService;
import com.litclub.Backend.service.low.UserBooksService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserBooksService userBooksService;

    private final BookMetadataService metadataService;

    public BookService(BookRepository bookRepository, UserBooksService userBooksService, BookMetadataService metadataService) {
        this.bookRepository = bookRepository;
        this.userBooksService = userBooksService;
        this.metadataService = metadataService;
    }

    // ====== CREATE ======
    @Transactional
    public Book createBook(Book book) {
        if (book.getTitle() != null && book.getPrimaryAuthor() != null) {
            return metadataService.enrichAndSaveByTitleAndAuthor(book.getTitle(), book.getPrimaryAuthor())
                    .orElseThrow(() -> new RuntimeException("failed to create book"));
        } else if (book.getIsbn() != null) {
            return metadataService.enrichAndSaveByIsbn(book.getIsbn())
                    .orElseThrow(() -> new RuntimeException("failed to create book"));
        } else {
            throw new MalformedDTOException("book title or isbn is null");
        }
    }

    // ===== READ ======
    @Transactional(readOnly = true)
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Book getBook(Long bookID) {
        var bookOpt = bookRepository.findBookByBookID(bookID);
        if (bookOpt.isEmpty()) {
            throw new BookNotFoundException("bookID", bookID.toString());
        }
        return bookOpt.get();
    }

    @Transactional(readOnly = true)
    public Book getBookByISBN(String isbn) {
        var bookOpt = bookRepository.findBookByisbn(isbn);
        if (bookOpt.isEmpty()) {
            throw new BookNotFoundException("isbn", isbn);
        }
        return bookOpt.get();
    }

    @Transactional(readOnly = true)
    public List<Book> getBookByTitle(String title) {
        List<Book> books = bookRepository.findAllByTitle(title);
        if (books.isEmpty()) {
            throw new BookNotFoundException("title", title);
        }
        return books;
    }

    @Transactional(readOnly = true)
    public Book getFirstBookByTitle(String title) {
        List<Book> books = getBookByTitle(title);
        return books.getFirst();
    }

    @Transactional(readOnly = true)
    public Book getBookByTitleAndPrimaryAuthor(String title, String author) {
        return bookRepository.findBookByTitleAndPrimaryAuthor(title, author).orElseThrow(
                () -> new BookNotFoundException("title/author", title + "/" + author)
        );
    }

    @Transactional(readOnly = true)
    public List<User> getUsersForBook(Map<String, Object> identifierMap) {
        Book book;
        if (identifierMap.containsKey("bookID")) {
            book = getBookByISBN((String) identifierMap.get("bookID"));
        } else if (identifierMap.containsKey("isbn")) {
            book = getBookByISBN((String) identifierMap.get("isbn"));
        } else if (identifierMap.containsKey("title") && !identifierMap.containsKey("primaryAuthor")) {
            book = getFirstBookByTitle((String) identifierMap.get("title"));
        } else if (identifierMap.containsKey("title") && identifierMap.containsKey("primaryAuthor")) {
            book = getBookByTitleAndPrimaryAuthor((String) identifierMap.get("title"), (String) identifierMap.get("primaryAuthor"));
        } else {
            throw new BookNotFoundException(identifierMap.keySet().toString(), identifierMap.values().toString());
        }

        return userBooksService.getUsersForBook(book);
    }

    // ====== UPDATE ======
    // ====== UPDATE ======
    @Transactional
    public Book updateBook(Map<String, String> identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new MalformedDTOException("identifier is null or empty");
        }

        if (identifier.containsKey("isbn")) {
            String isbn = identifier.get("isbn");
            if (isbn == null || isbn.isBlank()) {
                throw new MalformedDTOException("ISBN cannot be null or blank");
            }
            return updateBookByISBN(isbn);
        }

        boolean hasTitle = identifier.containsKey("title");
        boolean hasAuthor = identifier.containsKey("primaryAuthor");

        if (hasTitle && !hasAuthor) {
            throw new MalformedDTOException("primaryAuthor cannot be null if title is present");
        } else if (!hasTitle && hasAuthor) {
            throw new MalformedDTOException("title cannot be null if primaryAuthor is present");
        } else if (hasTitle && hasAuthor) {
            String title = identifier.get("title");
            String author = identifier.get("primaryAuthor");
            if (title == null || title.isBlank() || author == null || author.isBlank()) {
                throw new MalformedDTOException("title and primaryAuthor cannot be blank");
            }
            return updateBookByTitleAndAuthor(title, author);
        }

        throw new MalformedDTOException("identifier must include either {isbn} or {title, primaryAuthor}");
    }


    public Book updateBookByISBN(String isbn) {
        return metadataService.enrichAndSaveByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("failed to update book"));
    }

    public Book updateBookByTitleAndAuthor(String title, String author) {
        return metadataService.enrichAndSaveByTitleAndAuthor(title, author)
                .orElseThrow(() -> new RuntimeException("failed to update book"));
    }
}
