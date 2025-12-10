package com.opensourcereader.core.issue.dto;

public record IssueDto(Long tagId, Boolean isOpened, String title, String body) {}
