package com.litclub.construct.interfaces.library.book;

import com.litclub.construct.enums.BookStatus;

import java.time.LocalDate;
import java.util.List;

public record BookDTO(
        Long bookID,
        String title,
        List<String> authors,
        String isbn,
        String coverUrl,
        String publisher,
        LocalDate year,
        BookStatus status
) {
}
