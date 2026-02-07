package com.opensourcereader.core.activity.exception;

public class CommentNotFoundException extends BoardException {

  public CommentNotFoundException() {
    super(BoardErrorCode.COMMENT_NOT_FOUND);
  }
}
