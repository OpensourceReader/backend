package com.opensourcereader.core.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public abstract class OpenSourceReaderException extends RuntimeException {

  private final Instant timestamp;
  private final Map<String, Object> details;

  public OpenSourceReaderException(Map<String, Object> details) {
    this.timestamp = Instant.now();
    this.details = details;
  }

  public OpenSourceReaderException() {
    this.timestamp = Instant.now();
    this.details = new HashMap<>();
  }

  public OpenSourceReaderException addDetail(String key, Object value) {
    this.details.put(key, value);
    return this;
  }

  public OpenSourceReaderException addAllDetails(Map<String, Object> details) {
    this.details.putAll(details);
    return this;
  }
}
