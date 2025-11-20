package com.litclub.Backend.service.top.facilitator;

import com.litclub.Backend.construct.discussion.DiscussionThread;
import com.litclub.Backend.construct.note.NoteCreateRequest;
import com.litclub.Backend.construct.note.NoteWithReplies;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.InsufficientPermissionsException;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.middle.BookService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.ReplyService;
import com.litclub.Backend.service.middle.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DiscussionManagementService {

    private final DiscussionPromptService promptService;
    private final NoteService noteService;
    private final ReplyService replyService;
    private final ClubService clubService;
    private final BookService bookService;
    private final UserService userService;

    public DiscussionManagementService(
            DiscussionPromptService promptService,
            NoteService noteService,
            ReplyService replyService,
            ClubService clubService,
            BookService bookService,
            UserService userService) {
        this.promptService = promptService;
        this.noteService = noteService;
        this.replyService = replyService;
        this.clubService = clubService;
        this.bookService = bookService;
        this.userService = userService;
    }

    // ====== DiscussionPrompt ======
    @Transactional
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public DiscussionThread getDiscussionThread(Long clubID, Long promptID) {
        DiscussionPrompt prompt = promptService.findPromptById(promptID);
        if (!prompt.getClub().getClubID().equals(clubID)) {
            throw new AccessDeniedException("Prompt doesn't belong to this club");
        }
        return getDiscussionThread(promptID);
    }

    @Transactional
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public List<DiscussionThread> getDiscussionThreadForClub(long clubID) {
        Club club = clubService.requireClubById(clubID);
        List<DiscussionPrompt> prompts = promptService.findAllPromptsByClub(club);
        List<DiscussionThread> discussionThreads = new ArrayList<>();

        for (DiscussionPrompt prompt : prompts) {
            discussionThreads.add(getDiscussionThread(prompt.getPromptID()));
        }
        return discussionThreads;
    }

    // ====== Notes ======
    @Transactional
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or @userSecurity.isAdmin(authentication)")
    public Note createClubNote(Long clubID,
                               Long promptID,
                               NoteCreateRequest noteCreateRequest,
                               CustomUserDetails customUserDetails) {
        if (!clubID.equals(noteCreateRequest.clubID())) {
            throw new MalformedDTOException("Incorrect Club in Request");
        }
        Club club = clubService.requireClubById(clubID);
        DiscussionPrompt prompt = null;
        if (promptID != null) {
            prompt = promptService.findPromptById(promptID);
        }

        return noteService.save(
                customUserDetails.getUser(),
                bookService.getBook(noteCreateRequest.bookID()),
                noteCreateRequest.content(),
                Optional.ofNullable(club),
                Optional.ofNullable(prompt),
                false
        );
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUser(authentication, #userID)")
    public Note createPrivateNote(
            Long userID,
            NoteCreateRequest createRequest
    ) {
        User user = userService.requireUserById(userID);
        return noteService.save(
                user,
                bookService.getBook(createRequest.bookID()),
                createRequest.content(),
                Optional.empty(),
                Optional.empty(),
                true
        );
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUser(authentication, #userID)")
    public Note updateNote(Long userID, Long noteID, String content) {
        Note note = noteService.getNoteById(noteID);
        if (!note.getUser().equals(userService.requireUserById(userID))) {
            throw new AccessDeniedException("Note doesn't belong to this User");
        }
        return noteService.updateNote(content, noteID);
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public void deleteNote(Long userID, Long noteID) {
        User user = userService.requireUserById(userID);
        Note note = noteService.getNoteById(noteID);
        if (!note.getUser().equals(user) || !user.getGlobalRoles().contains(GlobalRole.ADMINISTRATOR)) {
            throw new InsufficientPermissionsException("Note doesn't belong to this User & they are not an administrator");
        }
        noteService.deleteNote(noteID);
    }

    // ====== replies ======
    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUser(authentication, #userID)")
    public Reply createReply(Long userID, Long noteID, String content) {
        User user = userService.requireUserById(userID);
        Note note = noteService.getNoteById(noteID);
        return replyService.createReply(user, note, content);
    }

    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUser(authentication, #userID)")
    public Reply updateReply(Long userID, Long replyID, String content) {
        User user = userService.requireUserById(userID);
        Reply reply = replyService.getReplyById(replyID);
        if (!reply.getUser().equals(user)) {
            throw new AccessDeniedException("Reply doesn't belong to this User");
        }
        return replyService.updateReplyContent(replyID, content);
    }
    @Transactional
    @PreAuthorize("@userSecurity.isCurrentUserOrAdmin(authentication, #userID)")
    public void deleteReply(Long userID, Long replyID) {
        User user = userService.requireUserById(userID);
        Reply reply = replyService.getReplyById(replyID);
        if (!reply.getUser().equals(user)) {
            throw new AccessDeniedException("Reply doesn't belong to this User");
        }
        replyService.deleteReply(replyID);
    }

    // ====== Threads
    @Transactional
    public List<Reply> getRepliesForNote(Long noteID) {
        Note note = noteService.getNoteById(noteID);
        return replyService.getRepliesForNote(note);
    }

    // ------ Utility ------
    @Transactional
    public NoteWithReplies getNoteWithReplies(long noteID) {
        Note note = noteService.getNoteById(noteID);
        List<Reply> replies = getRepliesForNote(noteID);

        return new NoteWithReplies(
                note,
                replies,
                replies.size()
        );
    }

    @Transactional
    public DiscussionThread getDiscussionThread(long promptID) {
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
