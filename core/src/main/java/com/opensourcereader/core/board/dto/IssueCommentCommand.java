package com.opensourcereader.core.board.dto;

import com.opensourcereader.core.board.entity.Issue;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class IssueCommentCommand extends CommentCommand {

  private Issue issue;
}
