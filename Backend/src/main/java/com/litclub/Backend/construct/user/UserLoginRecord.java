package com.litclub.Backend.construct.user;

import java.util.Optional;

public record UserLoginRecord (
        Optional<String> email,
        Optional<String> username,
        String password
){
}
