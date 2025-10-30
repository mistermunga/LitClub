package com.litclub.Backend.construct.club;

import com.litclub.Backend.config.ConfigurationManager;

public record ClubSettingsRequest(
        String name,
        String description,
        ConfigurationManager.ClubFlags flags
) {}
