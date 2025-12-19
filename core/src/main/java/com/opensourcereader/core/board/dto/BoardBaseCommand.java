package com.opensourcereader.core.board.dto;

import java.time.Instant;

import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.user.entity.User;

import lombok.Data;

@Data
public class BoardBaseCommand {
  private Long id;
  private Instant createdAt;
  private Instant updatedAt;
  private Long tagId;
  private User author;
  private OpenSourceRepo repo;
  private String title;
  private String body;
  private Boolean isOpened;
  private Long commentCount;
}
