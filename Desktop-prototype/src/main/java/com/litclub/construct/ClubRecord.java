package com.litclub.construct;

import com.litclub.construct.record.user.UserRecord;

import java.util.Set;

public record ClubRecord(Long clubID,
                         String name,
                         UserRecord administrator,
                         Set<UserRecord> members)
{

}
