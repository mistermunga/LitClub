package com.litclub.session.construct;

import java.util.Set;

public record ClubRecord(String name,
                         UserRecord administrator,
                         Set<UserRecord> members)
{

}
