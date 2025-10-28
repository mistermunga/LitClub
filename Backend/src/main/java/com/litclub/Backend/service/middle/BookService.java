package com.litclub.Backend.service.middle;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.repository.BookRepository;
import com.litclub.Backend.service.low.UserBooksService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserBooksService userBooksService;

    public BookService(BookRepository bookRepository, UserBooksService userBooksService) {
        this.bookRepository = bookRepository;
        this.userBooksService = userBooksService;
    }

    // ====== CREATE ======
    @Transactional
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }
}
