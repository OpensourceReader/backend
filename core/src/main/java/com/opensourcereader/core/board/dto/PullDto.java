package com.opensourcereader.core.board.dto;

public record PullDto(Long tagId, Boolean isOpened, String title, String body) {}
