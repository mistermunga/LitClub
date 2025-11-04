package com.litclub.Backend.repository;

import com.litclub.Backend.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<Note> findAllByDiscussionPrompt(DiscussionPrompt prompt);

    Page<Note> findAllByBook(Book book, Pageable pageable);
    Page<Note> findAllByClub(Club club, Pageable pageable);
    Page<Note> findAllByDiscussionPrompt(DiscussionPrompt prompt, Pageable pageable);

    Page<Note> findAllByBookAndIsPrivate(Book book, boolean isPrivate, Pageable pageable);
}
