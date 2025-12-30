package com.opensourcereader.core.board.dto;

public record IssueDto(Long tagId, Boolean isOpened, String title, String body) {}
