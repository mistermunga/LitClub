package com.litclub.Backend.construct.library.book;

public record BookSearchRequest (
        String title,
        String author,
        String isbn
) {

}
