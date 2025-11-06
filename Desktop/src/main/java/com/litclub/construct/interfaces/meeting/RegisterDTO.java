package com.litclub.construct.interfaces.meeting;

import com.litclub.construct.Meeting;
import com.litclub.construct.User;

public record RegisterDTO (
        Meeting meeting,
        User user,
        boolean attended,
        boolean late,
        boolean excused
){}
