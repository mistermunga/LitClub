package com.litclub.Backend.controller.club;

import com.litclub.Backend.config.ConfigurationManager;
import com.litclub.Backend.construct.club.ClubActivityReport;
import com.litclub.Backend.construct.club.ClubCreateRequest;
import com.litclub.Backend.construct.club.ClubDashboard;
import com.litclub.Backend.construct.club.ClubStatistics;
import com.litclub.Backend.construct.discussion.DiscussionThread;
import com.litclub.Backend.construct.meeting.MeetingCreateRequest;
import com.litclub.Backend.construct.meeting.MeetingUpdateRequest;
import com.litclub.Backend.construct.note.NoteCreateRequest;
import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.*;
import com.litclub.Backend.exception.MalformedDTOException;
import com.litclub.Backend.security.roles.ClubRole;
import com.litclub.Backend.security.roles.GlobalRole;
import com.litclub.Backend.security.userdetails.CustomUserDetails;
import com.litclub.Backend.service.low.ClubMembershipService;
import com.litclub.Backend.service.low.DiscussionPromptService;
import com.litclub.Backend.service.low.NoteService;
import com.litclub.Backend.service.middle.ClubService;
import com.litclub.Backend.service.middle.MeetingService;
import com.litclub.Backend.service.middle.ReplyService;
import com.litclub.Backend.service.middle.UserService;
import com.litclub.Backend.service.top.facilitator.ClubActivityService;
import com.litclub.Backend.service.top.facilitator.DiscussionManagementService;
import com.litclub.Backend.service.top.facilitator.util.ClubInviteGenerator;
import com.litclub.Backend.service.top.gatekeeper.AdminService;
import com.litclub.Backend.service.top.gatekeeper.ClubModService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;
    private final AdminService adminService;
    private final UserService userService;
    private final ConfigurationManager configuration;
    private final ClubModService clubModService;
    private final ClubMembershipService clubMembershipService;
    private final ClubActivityService clubActivityService;
    private final MeetingService meetingService;
    private final DiscussionPromptService discussionPromptService;
    private final DiscussionManagementService discussionManagementService;
    private final NoteService noteService;
    private final ReplyService replyService;
    private final ClubInviteGenerator clubInviteGenerator;

    public ClubController(ClubService clubService,
                          AdminService adminService,
                          ConfigurationManager configuration,
                          UserService userService,
                          ClubModService clubModService,
                          ClubMembershipService clubMembershipService,
                          ClubActivityService clubActivityService,
                          MeetingService meetingService,
                          DiscussionPromptService discussionPromptService,
                          DiscussionManagementService discussionManagementService,
                          NoteService noteService,
                          ReplyService replyService, ClubInviteGenerator clubInviteGenerator) {
        this.clubService = clubService;
        this.adminService = adminService;
        this.userService = userService;
        this.configuration = configuration;
        this.clubModService = clubModService;
        this.clubMembershipService = clubMembershipService;
        this.clubActivityService = clubActivityService;
        this.meetingService = meetingService;
        this.discussionPromptService = discussionPromptService;
        this.discussionManagementService = discussionManagementService;
        this.noteService = noteService;
        this.replyService = replyService;
        this.clubInviteGenerator = clubInviteGenerator;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<Club>> getClubs(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllClubs(pageable));
    }

    @GetMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Club> getClub(@PathVariable Long clubID) {
        return ResponseEntity.ok(clubService.requireClubById(clubID));
    }

    @GetMapping("/{clubID}/role")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubRole> getClubRole(
            @PathVariable Long clubID,
            @AuthenticationPrincipal CustomUserDetails cud
    ) {
        Club club = clubService.requireClubById(clubID);
        User user = cud.getUser();

        Set<ClubRole> roles = clubMembershipService.getRolesForUserInClub(user, club);

        ClubRole highestRole = roles.contains(ClubRole.OWNER) ? ClubRole.OWNER
                : roles.contains(ClubRole.MODERATOR) ? ClubRole.MODERATOR
                : ClubRole.MEMBER;

        return ResponseEntity.ok(highestRole);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Club> createClub(@Valid @RequestBody ClubCreateRequest clubRequest) {
        if (configuration.getInstanceSettings().clubCreationMode().equals(ConfigurationManager.ClubCreationMode.ADMIN_ONLY) &&
                SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .noneMatch(auth -> auth.getAuthority().equals("ROLE_ADMINISTRATOR"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        User creator = userService.requireUserById(clubRequest.creator().userID());
        Club club = new Club();
        club.setClubName(clubRequest.clubName());
        club.setDescription(clubRequest.description());
        club.setCreator(creator);

        Club registeredClub = clubService.registerClub(club, creator);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredClub);
    }

    @PutMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isOwner(authentication, clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Club> updateClub(@PathVariable Long clubID, @Valid @RequestBody ClubCreateRequest clubRequest) {
        Club club = clubService.requireClubById(clubID);
        return ResponseEntity.ok(clubService.updateClub(clubRequest, club));
    }

    @DeleteMapping("/{clubID}")
    @PreAuthorize("@clubSecurity.isOwner(authentication, clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteClub(@PathVariable Long clubID) {
        clubService.deleteClub(clubID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/members")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<UserRecord>> getClubMembers(@PathVariable Long clubID, @PageableDefault Pageable pageable) {
        List<User> members = clubService.getUsersForClub(clubID);
        Page<UserRecord> page = UserService.convertUserListToRecordPage(members, pageable);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/{clubID}/members")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<ClubMembership> addUserToClub(@PathVariable Long clubID, @RequestBody UserRecord userRecord) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubModService.addMember(clubID, userRecord.userID()));
    }

    @DeleteMapping("/{clubID}/members/{userID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID) or hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> removeUserFromClub(@PathVariable Long clubID, @PathVariable Long userID) {
        clubModService.removeMember(clubID, userID);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clubID}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ClubMembership> joinClub(@PathVariable Long clubID, @AuthenticationPrincipal CustomUserDetails cud) {
        boolean isClubOpen = configuration.getClubFlags(clubID).enableRegister();
        User user = cud.getUser();
        Club club = clubService.requireClubById(clubID);

        if (!isClubOpen && !user.getGlobalRoles().contains(GlobalRole.ADMINISTRATOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(clubMembershipService.enrollUserToClub(club, user));
    }

    @GetMapping("/{clubID}/invite")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<String> generateInvite(
            @PathVariable Long clubID,
            @AuthenticationPrincipal CustomUserDetails cud
    ) {
        return ResponseEntity.ok(clubModService.generateInvite(clubID, cud.getUserID()));
    }

    @PostMapping("/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ClubMembership> redeemInvite(
            @RequestBody String invite,
            @AuthenticationPrincipal CustomUserDetails cud) {
        ClubInviteGenerator.DecodedInvite decodedInvite = clubInviteGenerator.decodeInvite(invite);
        Club club = clubService.requireClubById(decodedInvite.clubID());
        User user = cud.getUser();
        return ResponseEntity
                .status(HttpStatus.CREATED).body(
                        clubMembershipService.enrollUserToClub(club, user)
                );
    }

    @PostMapping("/{clubID}/leave")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leaveClub(@PathVariable Long clubID, @AuthenticationPrincipal CustomUserDetails cud) {
        Club club = clubService.requireClubById(clubID);
        User user = cud.getUser();
        ClubMembership clubMembership = clubMembershipService.getMembershipByClubAndUser(club, user);
        clubMembershipService.deRegisterUserFromClub(clubMembership.getClubMembershipID());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/dashboard")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubDashboard> getClubDashboard(@PathVariable Long clubID) {
        return ResponseEntity.ok(clubActivityService.getClubDashboard(clubID));
    }

    @GetMapping("/{clubID}/activity")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubActivityReport> getClubActivityReport(@PathVariable Long clubID) {
        return ResponseEntity.ok(clubActivityService.getClubActivity(clubID));
    }

    @GetMapping("/{clubID}/activity/stats")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<ClubStatistics> getClubActivityStats(@PathVariable Long clubID) {
        return ResponseEntity.ok(clubActivityService.getClubStatistics(clubID));
    }

    @GetMapping("/{clubID}/meetings")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Page<Meeting>> getClubMeetings(@PathVariable Long clubID, @PageableDefault Pageable pageable) {
        Club club = clubService.requireClubById(clubID);
        Page<Meeting> meetings = meetingService.getMeetingsForClub(club, pageable);
        return ResponseEntity.ok(meetings);
    }

    @PostMapping("/{clubID}/meetings")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Meeting> addMeeting(@PathVariable Long clubID, @RequestBody MeetingCreateRequest createRequest,
                                              @AuthenticationPrincipal CustomUserDetails cud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clubModService.createMeeting(cud, clubID, createRequest));
    }

    @GetMapping("/{clubID}/meetings/{meetingID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Meeting> getMeeting(@PathVariable Long clubID, @PathVariable Long meetingID) {
        Meeting meeting = meetingService.requireById(meetingID);
        validateMeetingBelongsToClub(meeting, clubID);
        return ResponseEntity.ok(meeting);
    }

    @PostMapping("/{clubID}/meetings/{meetingID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Meeting> updateMeeting(@PathVariable Long clubID, @PathVariable Long meetingID,
                                                 @RequestBody MeetingUpdateRequest meetingUpdateRequest) {
        Meeting meeting = meetingService.requireById(meetingID);
        validateMeetingBelongsToClub(meeting, clubID);
        return ResponseEntity.ok(clubModService.updateMeeting(clubID, meetingID, meetingUpdateRequest));
    }

    @DeleteMapping("/{clubID}/meetings/{meetingID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long clubID, @PathVariable Long meetingID) {
        Meeting meeting = meetingService.requireById(meetingID);
        validateMeetingBelongsToClub(meeting, clubID);
        clubModService.deleteMeeting(clubID, meetingID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/discussions")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Page<DiscussionPrompt>> getDiscussionPrompts(@PathVariable Long clubID, Pageable pageable) {
        return ResponseEntity.ok(discussionPromptService.findAllPromptsByClub(clubService.requireClubById(clubID), pageable));
    }

    @PostMapping("/{clubID}/discussions")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<DiscussionPrompt> addDiscussionPrompt(@PathVariable Long clubID, @RequestBody String prompt,
                                                                @AuthenticationPrincipal CustomUserDetails cud) {
        return ResponseEntity.ok(discussionPromptService.createPrompt(prompt, cud.getUser(),
                clubService.requireClubById(clubID)));
    }

    @GetMapping("/{clubID}/discussions/{promptID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<DiscussionThread> getDiscussionThread(@PathVariable Long clubID, @PathVariable Long promptID) {
        DiscussionThread thread = discussionManagementService.getDiscussionThread(clubID, promptID);
        return ResponseEntity.ok(thread);
    }

    @DeleteMapping("/{clubID}/discussions/{promptID}")
    @PreAuthorize("@clubSecurity.isModerator(authentication, #clubID)")
    public ResponseEntity<Void> deleteDiscussionThread(@PathVariable Long clubID, @PathVariable Long promptID) {
        discussionPromptService.deletePrompt(clubID, promptID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/notes")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Page<Note>> getClubNotes(@PathVariable Long clubID, Pageable pageable) {
        Club club = clubService.requireClubById(clubID);
        return ResponseEntity.ok(noteService.getAllNotes(club, pageable));
    }

    @PostMapping("/{clubID}/notes")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Note> createClubNote(@PathVariable Long clubID, @RequestBody NoteCreateRequest noteCreateRequest,
                                               @AuthenticationPrincipal CustomUserDetails cud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(discussionManagementService.createClubNote(clubID, null, noteCreateRequest, cud));
    }

    @PutMapping("/{clubID}/notes/{noteID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Note> updateClubNote(@PathVariable Long clubID, @PathVariable Long noteID,
                                               @RequestBody NoteCreateRequest noteCreateRequest,
                                               @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = noteService.getNoteById(noteID);
        validateNoteBelongsToClub(note, clubID);
        return ResponseEntity.ok(discussionManagementService.updateNote(cud.getUserID(), noteID, noteCreateRequest.content()));
    }

    @DeleteMapping("/{clubID}/notes/{noteID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Void> deleteClubNote(@PathVariable Long clubID, @PathVariable Long noteID,
                                               @AuthenticationPrincipal CustomUserDetails cud) {
        Note note = noteService.getNoteById(noteID);
        validateNoteBelongsToClub(note, clubID);

        if (!note.getUser().equals(cud.getUser()) && !cud.getAuthorities().contains(GlobalRole.ADMINISTRATOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        noteService.deleteNote(noteID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/discussions/{promptID}/notes")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Page<Note>> getPromptNotes(@PathVariable Long clubID, @PathVariable Long promptID, Pageable pageable) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        return ResponseEntity.ok(noteService.getAllNotes(prompt, pageable));
    }

    @PostMapping("/{clubID}/discussions/{promptID}/notes")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Note> createPromptNote(@PathVariable Long clubID, @PathVariable Long promptID,
                                                 @RequestBody NoteCreateRequest noteCreateRequest,
                                                 @AuthenticationPrincipal CustomUserDetails cud) {
        validatePromptBelongsToClub(clubID, promptID);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(discussionManagementService.createClubNote(clubID, promptID, noteCreateRequest, cud));
    }

    @PutMapping("/{clubID}/discussions/{promptID}/notes/{noteID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Note> updatePromptNote(@PathVariable Long clubID, @PathVariable Long promptID,
                                                 @PathVariable Long noteID, @RequestBody NoteCreateRequest noteCreateRequest,
                                                 @AuthenticationPrincipal CustomUserDetails cud) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        Note note = noteService.getNoteById(noteID);
        validateNotePromptRelationship(note, prompt);
        return ResponseEntity.ok(discussionManagementService.updateNote(cud.getUserID(), noteID, noteCreateRequest.content()));
    }

    @DeleteMapping("/{clubID}/discussions/{promptID}/notes/{noteID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Void> deletePromptNote(@PathVariable Long clubID, @PathVariable Long promptID,
                                                 @PathVariable Long noteID, @AuthenticationPrincipal CustomUserDetails cud) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        Note note = noteService.getNoteById(noteID);
        validateNotePromptRelationship(note, prompt);

        if (!note.getUser().equals(cud.getUser()) && !cud.getAuthorities().contains(GlobalRole.ADMINISTRATOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        noteService.deleteNote(noteID);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clubID}/discussions/{promptID}/notes/{noteID}/replies")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Page<Reply>> getReplies(@PathVariable Long clubID, @PathVariable Long promptID,
                                                  @PathVariable Long noteID, Pageable pageable) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        Note note = noteService.getNoteById(noteID);
        validateNotePromptRelationship(note, prompt);
        return ResponseEntity.ok(replyService.getRepliesForNote(note, pageable));
    }

    @PostMapping("/{clubID}/discussions/{promptID}/notes/{noteID}/replies")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Reply> createReply(@PathVariable Long clubID, @PathVariable Long promptID,
                                             @PathVariable Long noteID, @RequestBody String content,
                                             @AuthenticationPrincipal CustomUserDetails cud) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        Note note = noteService.getNoteById(noteID);
        validateNotePromptRelationship(note, prompt);
        return ResponseEntity.status(HttpStatus.CREATED).body(replyService.createReply(cud.getUser(), note, content));
    }

    @PutMapping("/{clubID}/discussions/{promptID}/notes/{noteID}/replies/{replyID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Reply> updateReply(@PathVariable Long clubID, @PathVariable Long promptID,
                                             @PathVariable Long noteID, @PathVariable Long replyID,
                                             @RequestBody String content, @AuthenticationPrincipal CustomUserDetails cud) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        Note note = noteService.getNoteById(noteID);
        validateNotePromptRelationship(note, prompt);
        Reply reply = replyService.getReplyById(replyID);
        validateReplyNoteRelationship(reply, note);
        return ResponseEntity.ok(replyService.updateReplyContent(replyID, content));
    }

    @DeleteMapping("/{clubID}/discussions/{promptID}/notes/{noteID}/replies/{replyID}")
    @PreAuthorize("@clubSecurity.isMember(authentication, #clubID)")
    public ResponseEntity<Void> deleteReply(@PathVariable Long clubID, @PathVariable Long promptID,
                                            @PathVariable Long noteID, @PathVariable Long replyID,
                                            @AuthenticationPrincipal CustomUserDetails cud) {
        DiscussionPrompt prompt = validatePromptBelongsToClub(clubID, promptID);
        Note note = noteService.getNoteById(noteID);
        validateNotePromptRelationship(note, prompt);
        Reply reply = replyService.getReplyById(replyID);
        validateReplyNoteRelationship(reply, note);

        if (!reply.getUser().equals(cud.getUser()) && !cud.getAuthorities().contains(GlobalRole.ADMINISTRATOR)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        replyService.deleteReply(replyID);
        return ResponseEntity.noContent().build();
    }

    private void validateMeetingBelongsToClub(Meeting meeting, Long clubID) {
        if (!meeting.getClub().getClubID().equals(clubID)) {
            throw new MalformedDTOException("meeting does not belong to club");
        }
    }

    private DiscussionPrompt validatePromptBelongsToClub(Long clubID, Long promptID) {
        Club club = clubService.requireClubById(clubID);
        DiscussionPrompt prompt = discussionPromptService.findPromptById(promptID);
        if (!prompt.getClub().equals(club)) {
            throw new MalformedDTOException("prompt does not belong to club");
        }
        return prompt;
    }

    private void validateNoteBelongsToClub(Note note, Long clubID) {
        if (note.getDiscussionPrompt() != null && !note.getDiscussionPrompt().getClub().getClubID().equals(clubID)) {
            throw new MalformedDTOException("note does not belong to club");
        }
    }

    private void validateNotePromptRelationship(Note note, DiscussionPrompt prompt) {
        if (!note.getDiscussionPrompt().equals(prompt)) {
            throw new MalformedDTOException("note does not belong to prompt");
        }
    }

    private void validateReplyNoteRelationship(Reply reply, Note note) {
        if (!reply.getParentNote().equals(note)) {
            throw new MalformedDTOException("reply does not belong to note");
        }
    }
}