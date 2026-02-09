package com.opensourcereader.api.controller.board.response;

import java.time.Instant;

import com.opensourcereader.core.collaboration.entity.Issue;
import com.opensourcereader.core.collaboration.entity.Pull;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record BoardPreviewResponse(
    Long id,
    String title,
    Integer tagId,
    String body,
    UserDto userDto,
    Integer commentCount,
    Instant createdAt,
    Instant updatedAt) {

  public static BoardPreviewResponse of(Issue entity, UserDto userDto) {
    return BoardPreviewResponse.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .tagId(entity.getTagId())
        .body(entity.getBody())
        .userDto(userDto)
        .commentCount(entity.getCommentCount())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public static BoardPreviewResponse of(Pull entity, UserDto userDto) {
    return BoardPreviewResponse.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .tagId(entity.getTagId())
        .body(entity.getBody())
        .userDto(userDto)
        .commentCount(entity.getCommentCount() + entity.getReviewCount())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
}
