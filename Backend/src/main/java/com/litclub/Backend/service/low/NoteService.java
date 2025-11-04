package com.litclub.Backend.service.low;

import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.repository.NoteRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // ====== CREATE ======
    @Transactional
    public Note save(User user,
                     Book book,
                     String content,
                     Optional<Club> club,
                     Optional<DiscussionPrompt> prompt,
                     boolean isPrivate) {

        if (isPrivate) {

            if (noteRepository.existsByUserAndBookAndContent(user, book, content)) {
                throw new EntityExistsException("Note already exists");
            }

        } else if (club.isEmpty()) {
            throw new MalformedDTOException("Club is empty");
        } else if (noteRepository.existsByUserAndBookAndContentAndClub(user, book, content, club.get())) {
            throw new EntityExistsException("Note already exists");
        }

        Note note = new Note();
        note.setUser(user);
        note.setBook(book);
        if (!isPrivate) note.setClub(club.get());
        note.setContent(content);
        note.setPrivate(isPrivate);
        prompt.ifPresent(note::setDiscussionPrompt);

        return noteRepository.save(note);
    }

    // ====== READ ======
    @Transactional(readOnly = true)
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Note getNoteById(long noteID) {
        return noteRepository.findNoteByNoteID(noteID)
                .orElseThrow(() -> new EntityNotFoundException("Note not found"));
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(User user) {
        return noteRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(Book book) {
        return noteRepository.findAllByBook(book);
    }

    @Transactional(readOnly = true)
    public Page<Note> getAllNotes(Book book, Pageable pageable) {
        return noteRepository.findAllByBook(book, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Note> getAllNotes(Book book, Pageable pageable, boolean isAdmin) {
        if (isAdmin) {
            return getAllNotes(book, pageable);
        } else {
            return noteRepository.findAllByBookAndIsPrivate(book, false, pageable); // return public notes
        }
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(Club club) {
        return noteRepository.findAllByClub(club);
    }

    @Transactional(readOnly = true)
    public Page<Note> getAllNotes(Club club, Pageable pageable) {
        return noteRepository.findAllByClub(club, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Note> getAllNotes(DiscussionPrompt prompt, Pageable pageable) {
        return noteRepository.findAllByDiscussionPrompt(prompt, pageable);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(User user, Book book) {
        return noteRepository.findAllByUserAndBook(user, book);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(User user, Club club) {
        return noteRepository.findAllByUserAndClub(user, club);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(Book book, Club club) {
        return noteRepository.findAllByBookAndClub(book, club);
    }

    @Transactional(readOnly = true)
    public List<Note> getAllNotes(DiscussionPrompt prompt) { return noteRepository.findAllByDiscussionPrompt(prompt); }

    @Transactional(readOnly = true)
    public List<Note> searchNotes(User user, String content) {
        return noteRepository.findAllByUserAndContentContaining(user, content);
    }

    // ====== UPDATE ======
    @Transactional
    public Note updateNote(String content, Long noteID) {
        if (noteID == null) {
            throw new MalformedDTOException("Note ID is null");
        }
        Note note = getNoteById(noteID);
        note.setContent(content);
        return noteRepository.save(note);
    }

    // ====== DELETE ======
    @Transactional
    public void deleteNote(Long noteID) {
        if (noteID == null) {
            throw new MalformedDTOException("Note ID is null");
        }
        noteRepository.deleteById(noteID);
    }
}
