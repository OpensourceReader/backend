package com.opensourcereader.core.collaboration.dto;

import java.time.temporal.ChronoUnit;

import com.opensourcereader.core.collaboration.entity.IssueComment;
import com.opensourcereader.core.user.dto.UserDto;

import lombok.Builder;

@Builder
public record IssueCommentDto(UserDto author, String createdAt, String updatedAt, String body) {

  public static IssueCommentDto of(UserDto author, IssueComment comment) {
    // time format : yyyy-MM-ddTHH:mm:ss.sssZ
    String createdAtToString = comment.getCreatedAt().truncatedTo(ChronoUnit.SECONDS).toString();
    String updatedAtToString = comment.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS).toString();

    return IssueCommentDto.builder()
        .author(author)
        .createdAt(createdAtToString)
        .updatedAt(updatedAtToString)
        .body(comment.getBody())
        .build();
  }
}
