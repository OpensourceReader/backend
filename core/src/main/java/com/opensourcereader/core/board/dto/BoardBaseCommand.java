package com.opensourcereader.core.board.dto;

import java.time.Instant;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
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
public class BoardBaseCommand {
  private Long id;
  private User author;
  private OpenSourceRepo repo;
  private Integer tagId;
  private Instant createdAt;
  private Instant updatedAt;
  private String title;
  private String body;
  private Boolean isOpened;
  private Integer commentCount;
}
