package com.opensourcereader.core.collaboration.exception;

public class CommentNotFoundException extends BoardException {

  public CommentNotFoundException() {
    super(BoardErrorCode.COMMENT_NOT_FOUND);
  }
}
