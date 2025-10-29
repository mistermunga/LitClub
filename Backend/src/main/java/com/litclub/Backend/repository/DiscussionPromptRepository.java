package com.litclub.Backend.repository;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.DiscussionPrompt;
import com.litclub.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscussionPromptRepository extends JpaRepository<DiscussionPrompt, Long> {

    boolean existsByPromptAndClub(String prompt, Club club);

    Optional<DiscussionPrompt> findByPromptID(Long promptID);
    Optional<DiscussionPrompt> findByPromptAndClub(String prompt, Club club);
    List<DiscussionPrompt> findAllByClub(Club club);
    List<DiscussionPrompt> findAllByPoster(User poster);
    List<DiscussionPrompt> findAllByPosterAndClub(User poster, Club club);

}
