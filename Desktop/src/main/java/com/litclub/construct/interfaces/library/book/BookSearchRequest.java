package com.litclub.construct.interfaces.library.book;

public record BookSearchRequest (
        String title,
        String author,
        String isbn
) {

}
