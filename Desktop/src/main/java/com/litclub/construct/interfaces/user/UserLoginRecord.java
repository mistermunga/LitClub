package com.litclub.construct.interfaces.user;

import java.util.Optional;

public record UserLoginRecord (
        Optional<String> email,
        Optional<String> username,
        String password
){
}
