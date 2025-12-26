package com.opensourcereader.api.controller.board.response;

import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record BoardBaseResponse(Long id, Long tagId, String title, UserDto author) {

  public static BoardBaseResponse ofIssue(Issue entity, UserDto user) {
    return BoardBaseResponse.builder()
        .id(entity.getId())
        .tagId(entity.getTagId())
        .title(entity.getTitle())
        .author(user)
        .build();
  }

  public static BoardBaseResponse ofPull(Pull entity, UserDto user) {
    return BoardBaseResponse.builder()
        .id(entity.getId())
        .tagId(entity.getTagId())
        .title(entity.getTitle())
        .author(user)
        .build();
  }
}
