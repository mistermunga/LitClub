package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club,Long> {
}
