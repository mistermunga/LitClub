package com.litclub.Backend.service.low;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubBook;
import com.litclub.Backend.repository.ClubBookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClubBookService {

    private final ClubBookRepository clubBookRepository;

    public ClubBookService(ClubBookRepository clubBookRepository) {
        this.clubBookRepository = clubBookRepository;
    }

    @Transactional
    public ClubBook createClubBook(Club club, Book book) {
        var clubBook = clubBookRepository.findClubBookByClubAndBook(club, book);
        return clubBook.orElseGet(() -> clubBookRepository.save(new ClubBook(club, book)));
    }

    @Transactional(readOnly = true)
    public ClubBook getClubBook(Club club, Book book) {
        return clubBookRepository.findClubBookByClubAndBook(club, book)
                .orElseThrow(() -> new EntityNotFoundException("ClubBook not found"));
    }

    @Transactional(readOnly = true)
    public List<ClubBook> getValidClubBooks(Club club) {
        return clubBookRepository.findAllByClubAndValid(club, true);
    }

    @Transactional
    public ClubBook updateClubBook(Club club, Book book, boolean value) {
        ClubBook clubBook = getClubBook(club, book);
        clubBook.setValid(value);
        return clubBookRepository.save(clubBook);
    }

    @Transactional
    public void deleteClubBook(Club club, Book book) {
        getClubBook(club, book);
        clubBookRepository.deleteByClubAndBook(club, book);
    }

}
