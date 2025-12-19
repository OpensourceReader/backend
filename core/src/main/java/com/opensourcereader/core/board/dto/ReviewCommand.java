package com.opensourcereader.core.board.dto;

import com.opensourcereader.core.board.entity.Pull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewCommand extends CommentCommand {

  private Pull pull;
}
