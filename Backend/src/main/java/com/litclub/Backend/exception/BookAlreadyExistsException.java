package com.litclub.Backend.exception;

import com.litclub.Backend.entity.Book;
import jakarta.persistence.EntityExistsException;

import java.util.HashMap;
import java.util.Map;

public class BookAlreadyExistsException extends EntityExistsException {
    private final Map<String, String> details;

    public BookAlreadyExistsException(Book book) {
        super("Book already exists: " + book.getTitle() + " (" + book.getBookID() + ")");
        details = new HashMap<>();
        details.put("bookID", String.valueOf(book.getBookID()));
        details.put("title", book.getTitle());
        details.put("authors", book.getAuthorsAsString());
        details.put("publisher", book.getPublisher());
        details.put("year", String.valueOf(book.getYear()));
        details.put("isbn", String.valueOf(book.getIsbn()));
    }

    public Map<String, String> getDetails() {
        return details;
    }
}

