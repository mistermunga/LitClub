package com.litclub.construct;

import java.util.Objects;

public class Book {

    private int bookID;
    private String title;
    private String author;
    private String isbn;
    private String coverURL;

    public Book() {}

    public Book(int bookID, String title, String author, String isbn, String coverURL) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.coverURL = coverURL;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookID, title, author, isbn, coverURL);
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }
}
