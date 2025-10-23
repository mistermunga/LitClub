package com.litclub.Backend.repository;

import com.litclub.Backend.entity.UserBooks;
import com.litclub.Backend.entity.compositeKey.UserBooksID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBooksRepository extends JpaRepository<UserBooks, UserBooksID> {
}
