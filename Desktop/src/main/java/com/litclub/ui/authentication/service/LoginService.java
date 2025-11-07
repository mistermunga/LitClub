package com.litclub.ui.authentication.service;

import com.litclub.construct.interfaces.user.UserLoginRecord;
import com.litclub.persistence.repository.LibraryRepository;

import java.util.Optional;
import java.util.regex.Pattern;

public class LoginService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{1,64}@)[\\p{L}\\p{N}._%+-]+@[\\p{L}\\p{N}.-]+\\.[\\p{L}]{2,}$",
            Pattern.UNICODE_CASE
    );

    private final UserLoginRecord userLoginRecord;

    public LoginService(String identifier, String password) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (isValidEmail(identifier)) {
            // identifier is an email
            userLoginRecord = new UserLoginRecord(Optional.of(identifier), Optional.empty(), password);
        } else {
            // identifier is a username
            userLoginRecord = new UserLoginRecord(Optional.empty(), Optional.of(identifier), password);
        }
    }

    public void login() {
        LibraryRepository.getInstance().login(userLoginRecord);
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}

