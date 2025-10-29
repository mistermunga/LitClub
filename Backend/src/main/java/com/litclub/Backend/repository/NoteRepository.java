package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.Note;
import com.litclub.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    boolean existsByUserAndBookAndContent(User user, Book book, String content);
    boolean existsByUserAndBookAndContentAndClub(User user, Book book, String content, Club club);

    Optional<Note> findNoteByNoteID(long noteID);

    List<Note> findAllByUser(User user);
    List<Note> findAllByBook(Book book);
    List<Note> findAllByClub(Club club);
    List<Note> findAllByUserAndBook(User user, Book book);
    List<Note> findAllByUserAndClub(User user, Club club);
    List<Note> findAllByBookAndClub(Book book, Club club);
    List<Note> findAllByUserAndContentContaining(User user, String content);

}
