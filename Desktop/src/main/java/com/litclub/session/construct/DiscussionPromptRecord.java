package com.litclub.session.construct;

import java.time.LocalDateTime;

public record DiscussionPromptRecord(
        int id,
        int posterID,
        String content,
        LocalDateTime postedAt
){
}
