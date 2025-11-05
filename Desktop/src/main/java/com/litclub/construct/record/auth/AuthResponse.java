package com.litclub.construct.record.auth;

import com.litclub.construct.record.user.UserRecord;

public record AuthResponse(String token, UserRecord userRecord) {}
