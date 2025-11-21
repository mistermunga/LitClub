package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Book;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.ClubBook;
import com.litclub.Backend.entity.compositeKey.ClubBookID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubBookRepository extends JpaRepository<ClubBook, ClubBookID> {

    Optional<ClubBook> findClubBookByClubAndBook(Club club, Book book);
    List<ClubBook> findAllByClubAndValid(Club club, boolean valid);
    void deleteByClubAndBook(Club club, Book book);

}
