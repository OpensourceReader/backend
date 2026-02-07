package com.opensourcereader.core.activity.dto;

import com.opensourcereader.core.activity.entity.Issue;

public record PairIssue(Issue issue, BoardBaseCommand command) {}
