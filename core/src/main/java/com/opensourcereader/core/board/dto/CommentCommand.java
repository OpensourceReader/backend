package com.opensourcereader.core.board.dto;

import java.time.Instant;

import com.opensourcereader.core.user.entity.User;

import lombok.Data;

@Data
public class CommentCommand {
  private Long id;
  private Instant createdAt;
  private Instant updatedAt;
  private User author;
  private String body;
}
