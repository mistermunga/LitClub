package com.litclub.Backend.construct.library;

import com.litclub.Backend.construct.library.book.BookStatus;

public record BookAddRequest(
        String title,
        String author,
        String isbn,
        BookStatus initialStatus
) {}
