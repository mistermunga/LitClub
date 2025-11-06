package com.litclub.construct.record.auth;

import java.util.Optional;

public record UserLoginRecord(
        Optional<String> email,
        Optional<String> username,
        String password
){
}
