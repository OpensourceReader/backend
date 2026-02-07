package com.opensourcereader.core.collaboration.dto;

import java.time.Instant;

import com.opensourcereader.core.user.entity.User;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class CommentCommand {
  private Long id;
  private Instant createdAt;
  private Instant updatedAt;
  private User author;
  private String body;
}
