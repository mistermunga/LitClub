package com.litclub.Backend.construct.library;

import com.litclub.Backend.construct.book.BookStatus;
import com.litclub.Backend.entity.Book;

import java.time.LocalDate;

public record BookWithStatus(
        Book book,
        BookStatus status,
        Integer rating,
        LocalDate dateStarted,
        LocalDate dateFinished
) {}
