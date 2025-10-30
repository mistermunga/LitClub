package com.litclub.Backend.construct.note;

import com.litclub.Backend.entity.Reply;
import com.litclub.Backend.entity.Note;
import java.util.List;

public record NoteWithReplies(
        Note note,
        List<Reply> replies,
        int replyCount
) {
}
