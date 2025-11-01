package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.construct.discussion.DiscussionThread;
import com.litclub.Backend.construct.note.NoteWithReplies;
import com.litclub.Backend.entity.Club;
import com.litclub.Backend.entity.DiscussionPrompt;
import com.litclub.Backend.entity.Note;
import com.litclub.Backend.entity.Reply;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.ReplyService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiscussionManagementService {

    private final DiscussionPromptService promptService;
    private final NoteService noteService;
    private final ReplyService replyService;
    private final ClubService clubService;

    public DiscussionManagementService(
            DiscussionPromptService promptService,
            NoteService noteService,
            ReplyService replyService,
            ClubService clubService
    ) {
        this.promptService = promptService;
        this.noteService = noteService;
        this.replyService = replyService;
        this.clubService = clubService;
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public DiscussionThread getDiscussionThread(Long clubID, Long promptID) {
        DiscussionPrompt prompt = promptService.findPromptById(promptID);
        if (!prompt.getClub().getClubID().equals(clubID)) {
            throw new AccessDeniedException("Prompt doesn't belong to this club");
        }
        return getDiscussionThread(promptID);
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public List<DiscussionThread> getDiscussionThreadForClub(long clubID) {
        Club club = clubService.requireClubById(clubID);
        List<DiscussionPrompt> prompts = promptService.findAllPromptsByClub(club);
        List<DiscussionThread> discussionThreads = new ArrayList<>();

        for (DiscussionPrompt prompt : prompts) {
            discussionThreads.add(getDiscussionThread(prompt.getPromptID()));
        }
        return discussionThreads;
    }

    // ------ Utility ------
    private NoteWithReplies getNoteWithReplies(long noteID) {
        Note note = noteService.getNoteById(noteID);
        List<Reply> replies = replyService.getRepliesForNote(note);

        return new NoteWithReplies(
                note,
                replies,
                replies.size()
        );
    }

    private DiscussionThread getDiscussionThread(long promptID) {
        DiscussionPrompt prompt = promptService.findPromptById(promptID);
        List<Note> notes = noteService.getAllNotes(prompt);

        List<NoteWithReplies> notesWithReplies = notes.stream()
                .map(note -> getNoteWithReplies(note.getNoteID()))
                .toList();

        int totalReplies = notesWithReplies.stream()
                .mapToInt(nwr -> nwr.replies().size())
                .sum();

        return new DiscussionThread(
                prompt,
                notesWithReplies,
                notes.size(),
                totalReplies
        );
    }

}
