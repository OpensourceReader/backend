package com.opensourcereader.core.board.exception;

public class BoardNotFoundException extends BoardException {

  public BoardNotFoundException() {
    super(BoardErrorCode.BOARD_NOT_FOUND);
  }
}
