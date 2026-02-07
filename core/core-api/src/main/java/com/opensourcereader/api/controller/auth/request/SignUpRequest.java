package com.opensourcereader.api.controller.auth.request;

public record SignUpRequest(String email, String password, String loginName) {}
