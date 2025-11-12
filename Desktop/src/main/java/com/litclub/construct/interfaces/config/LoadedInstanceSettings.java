package com.litclub.construct.interfaces.config;

public record LoadedInstanceSettings(
        ConfigurationManager.InstanceSettings instanceSettings,
        Boolean isAdmin
) {}
