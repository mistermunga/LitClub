package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.DiscussionPrompt;
import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.Note;

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
