package com.litclub.construct.interfaces.library;

import com.litclub.construct.enums.BookStatus;

public record BookAddRequest(
        String title,
        String author,
        String isbn,
        BookStatus initialStatus
) {}
