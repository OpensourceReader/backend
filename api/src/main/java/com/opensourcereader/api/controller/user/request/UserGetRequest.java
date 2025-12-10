package com.opensourcereader.api.controller.user.request;

import jakarta.validation.constraints.NotNull;

public record UserGetRequest(@NotNull String nickname) {}
