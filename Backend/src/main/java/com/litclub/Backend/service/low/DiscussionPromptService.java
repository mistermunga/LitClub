package com.litclub.Backend.service.low;

import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.DiscussionPrompt;
import com.litclub.Backend.entity.User;
import com.litclub.Backend.repository.DiscussionPromptRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiscussionPromptService {

    private final DiscussionPromptRepository discussionPromptRepository;

    public DiscussionPromptService(DiscussionPromptRepository discussionPromptRepository) {
        this.discussionPromptRepository = discussionPromptRepository;
    }

    // ====== CREATE ======
    @Transactional
    public DiscussionPrompt createPrompt(String prompt, User user, Club club) {
        if (discussionPromptRepository.existsByPromptAndClub(prompt, club)) {
            throw new EntityExistsException("Discussion prompt already exists");
        }

        DiscussionPrompt discussionPrompt = new DiscussionPrompt();

        discussionPrompt.setPrompt(prompt);
        discussionPrompt.setPoster(user);
        discussionPrompt.setClub(club);

        return discussionPromptRepository.save(discussionPrompt);
    }

    // ====== READ ======
    @Transactional(readOnly = true)
    public List<DiscussionPrompt> findAllPrompts(){
        return discussionPromptRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DiscussionPrompt findPromptById(long promptID) {
        return discussionPromptRepository.findByPromptID(promptID)
                .orElseThrow(() -> new EntityNotFoundException("Discussion prompt not found"));
    }

    @Transactional(readOnly = true)
    public List<DiscussionPrompt> findByUserAndClub(User user, Club club) {
        return discussionPromptRepository.findAllByPosterAndClub(user, club);
    }

    @Transactional(readOnly = true)
    public DiscussionPrompt findByPromptAndClub(String prompt, Club club) {
        return discussionPromptRepository.findByPromptAndClub(prompt, club)
                .orElseThrow(() -> new EntityNotFoundException("Discussion prompt not found"));
    }

    @Transactional(readOnly = true)
    public List<DiscussionPrompt> findAllPromptsByClub(Club club) {
        return discussionPromptRepository.findAllByClub(club);
    }

    @Transactional(readOnly = true)
    public List<DiscussionPrompt> findAllByPoster(User poster) {
        return discussionPromptRepository.findAllByPoster(poster);
    }

    // ====== DELETE ======
    @Transactional
    public void deleteByPromptID(long promptID) {
        var prompt = discussionPromptRepository.findByPromptID(promptID);
        prompt.ifPresent(discussionPromptRepository::delete);
    }

    @Transactional
    public void deleteByPromptAndClub(String prompt, Club club) {
        var discussionPrompt = discussionPromptRepository.findByPromptAndClub(prompt, club);
        discussionPrompt.ifPresent(discussionPromptRepository::delete);
    }

    @Transactional
    public void purgeClubPrompts(Club club) {
        List<DiscussionPrompt> prompts = discussionPromptRepository.findAllByClub(club);
        discussionPromptRepository.deleteAll(prompts);
    }

    @Transactional
    public void purgeUserPrompts(User user) {
        List<DiscussionPrompt> prompts = discussionPromptRepository.findAllByPoster(user);
        discussionPromptRepository.deleteAll(prompts);
    }

}
