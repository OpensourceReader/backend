package com.opensourcereader.core.collaboration.exception;

public class PullNotFoundException extends BoardException {

  public PullNotFoundException() {
    super(BoardErrorCode.PULL_NOT_FOUND);
  }
}
