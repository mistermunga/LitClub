package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findBookByisbn(String isbn);
    Optional<Book> findBookByTitleAndPrimaryAuthor(String title, String author);

    List<Book> findAllByTitle(String title);

    Optional<Book> findBookByBookID(long bookID);

    List<Book> findBookByTitleContainingIgnoreCase(String title);
    List<Book> findBookByisbnContainingIgnoreCase(String isbn);
    List<Book> findBookByPrimaryAuthorContainingIgnoreCase(String author);

}
