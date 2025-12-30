package com.opensourcereader.core.board.exception;

public class PullNotFoundException extends BoardException {

  public PullNotFoundException() {
    super(BoardErrorCode.PULL_NOT_FOUND);
  }
}
