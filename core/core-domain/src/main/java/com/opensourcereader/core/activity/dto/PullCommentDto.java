package com.opensourcereader.core.activity.dto;

import java.time.temporal.ChronoUnit;

import com.opensourcereader.core.activity.entity.PullComment;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record PullCommentDto(
    Long pullCommentId,
    String createdAt,
    String updatedAt,
    UserDto commentAuthor,
    String diffHunk,
    String body,
    String path) {

  public static PullCommentDto of(PullComment pullComment, UserDto commentAuthor) {

    // time format : yyyy-MM-ddTHH:mm:ss.sssZ
    String createdAtToString =
        pullComment.getCreatedAt().truncatedTo(ChronoUnit.SECONDS).toString();
    String updatedAtToString =
        pullComment.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS).toString();

    return PullCommentDto.builder()
        .pullCommentId(pullComment.getId())
        .createdAt(createdAtToString)
        .updatedAt(updatedAtToString)
        .commentAuthor(commentAuthor)
        .diffHunk(pullComment.getDiffHunk())
        .body(pullComment.getBody())
        .path(pullComment.getPath())
        .build();
  }
}
