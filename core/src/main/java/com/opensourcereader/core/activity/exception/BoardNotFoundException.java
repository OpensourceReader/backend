package com.opensourcereader.core.activity.exception;

public class BoardNotFoundException extends BoardException {

  public BoardNotFoundException() {
    super(BoardErrorCode.BOARD_NOT_FOUND);
  }
}
