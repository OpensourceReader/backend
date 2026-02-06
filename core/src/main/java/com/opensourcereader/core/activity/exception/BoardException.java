package com.opensourcereader.core.activity.exception;

import com.opensourcereader.core.shared.exception.OpenSourceReaderException;

import lombok.Getter;

@Getter
public abstract class BoardException extends OpenSourceReaderException {

  public BoardException(BoardErrorCode boardErrorCode) {
    super(boardErrorCode);
  }
}
