package com.litclub.Backend.construct.meeting;

import com.litclub.Backend.entity.Meeting;
import com.litclub.Backend.entity.User;

public record RegisterDTO (
        Meeting meeting,
        User user,
        boolean attended,
        boolean late,
        boolean excused
){}
