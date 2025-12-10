package com.opensourcereader.core.security.dto;

public record SignUpCommand(String email, String password, String username) {}
