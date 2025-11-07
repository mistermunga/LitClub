package com.litclub.construct.interfaces.auth;

import com.litclub.construct.interfaces.user.UserRecord;

public record AuthResponse(String token, UserRecord userRecord) {}
