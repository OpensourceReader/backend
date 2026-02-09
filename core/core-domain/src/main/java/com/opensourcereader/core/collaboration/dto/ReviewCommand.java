package com.opensourcereader.core.collaboration.dto;

import java.time.Instant;

import com.opensourcereader.core.collaboration.entity.Pull;
import com.opensourcereader.core.user.entity.User;

import lombok.Builder;

@Builder
public record ReviewCommand(Long id, Instant submittedAt, User author, String body, Pull pull) {}
