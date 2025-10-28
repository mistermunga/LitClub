package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findBookByisbn(String isbn);
    Optional<Book> findBookByTitleAndPrimaryAuthor(String title, String author);
}
