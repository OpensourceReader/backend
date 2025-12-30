package com.opensourcereader.core.board.dto;

import com.opensourcereader.core.board.entity.Review;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PullCommentCommand extends CommentCommand {

  private Review review;

  private String diffHunk;

  private String path;
}
