package com.opensourcereader.core.board.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum State {
  OPEN("open"),
  CLOSE("closed"),
  ALL("all"),
  UNKNOWN("unknown");

  private final String value;

  @JsonCreator
  public static State fromValue(String value) {
    return Arrays.stream(State.values())
        .filter(status -> status.value.equalsIgnoreCase(value))
        .findFirst()
        .orElse(UNKNOWN);
  }
}
