package com.litclub.construct.interfaces.library;



import com.litclub.construct.Book;
import com.litclub.construct.enums.BookStatus;

import java.time.LocalDate;

public record BookWithStatus(
        Book book,
        BookStatus status,
        Integer rating,
        LocalDate dateStarted,
        LocalDate dateFinished
) {}
