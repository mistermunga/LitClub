package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClubCreateRequest (
        @NotBlank(message = "Club name is required")
        @Size(max = 100, message = "Club name must not exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_]+$", message = "Club name contains invalid characters")
        String clubName,

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull
        UserRecord creator
) {
}
