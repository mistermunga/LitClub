package com.litclub.Backend.construct.meeting;

import com.litclub.Backend.config.ConfigurationManager;

public record ClubSettingsRequest(
        String name,
        String description,
        ConfigurationManager.ClubFlags flags
) {}
