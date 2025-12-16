package com.opensourcereader.api.controller.board.response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.opensourcereader.core.board.entity.Issue;
import com.opensourcereader.core.board.entity.Pull;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record BoardPreviewResponse(
    Long id,
    String title,
    Long tagId,
    String body,
    UserDto userDto,
    Long commentCount,
    String createdAt,
    String updatedAt) {

  public static BoardPreviewResponse of(Issue issue, UserDto userDto, Long commentCount) {
    return getBoardPreviewResponse(
        userDto,
        commentCount,
        issue.getCreatedAt(),
        issue.getUpdatedAt(),
        issue.getId(),
        issue.getTitle(),
        issue.getTagId(),
        issue.getBody());
  }

  public static BoardPreviewResponse of(Pull pull, UserDto userDto, Long commentCount) {
    return getBoardPreviewResponse(
        userDto,
        commentCount,
        pull.getCreatedAt(),
        pull.getUpdatedAt(),
        pull.getId(),
        pull.getTitle(),
        pull.getTagId(),
        pull.getBody());
  }

  private static BoardPreviewResponse getBoardPreviewResponse(
      UserDto userDto,
      Long commentCount,
      Instant createdAt,
      Instant updatedAt,
      Long id,
      String title,
      Long tagId,
      String body) {
    // time format : yyyy-MM-ddTHH:mm:ss.sssZ
    String createdAtToString = createdAt.truncatedTo(ChronoUnit.SECONDS).toString();
    String updatedAtToString = updatedAt.truncatedTo(ChronoUnit.SECONDS).toString();

    return BoardPreviewResponse.builder()
        .id(id)
        .title(title)
        .tagId(tagId)
        .body(body)
        .userDto(userDto)
        .commentCount(commentCount)
        .createdAt(createdAtToString)
        .updatedAt(updatedAtToString)
        .build();
  }
}
