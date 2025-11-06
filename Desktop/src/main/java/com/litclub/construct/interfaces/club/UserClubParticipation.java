package com.litclub.construct.interfaces.club;

import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Meeting;
import com.litclub.construct.Note;
import com.litclub.construct.interfaces.user.UserRecord;

import java.time.LocalDateTime;
import java.util.List;

public record UserClubParticipation(
        UserRecord user,
        ClubRecord club,
        List<Meeting> meetingsAttended,
        List<Note> notesPosted,
        List<DiscussionPrompt> promptsCreated,
        LocalDateTime joinedAt
) {}
