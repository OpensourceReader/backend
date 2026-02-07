package com.opensourcereader.core.collaboration.dto;

import com.opensourcereader.core.collaboration.entity.Issue;

public record PairIssue(Issue issue, BoardBaseCommand command) {}
