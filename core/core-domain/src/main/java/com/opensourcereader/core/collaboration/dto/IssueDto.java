package com.opensourcereader.core.collaboration.dto;

public record IssueDto(Long tagId, Boolean isOpened, String title, String body) {}
