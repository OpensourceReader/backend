package com.opensourcereader.core.board.exception;

import com.opensourcereader.core.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class BoardException extends OpenSourceReaderException {

  private final BoardErrorCode boardErrorCode;

  public BoardException(BoardErrorCode boardErrorCode) {
    this.boardErrorCode = boardErrorCode;
  }
}
