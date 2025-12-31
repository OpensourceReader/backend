package com.opensourcereader.core.analysis.dto;

import com.opensourcereader.core.analysis.entity.repo.ContentType;

public record OpenSourceFileInfo(String path, ContentType contentType, String rawText) {

  public static OpenSourceFileInfo of(String path, String typeNumber, String rawText) {
    return new OpenSourceFileInfo(path, ContentType.from(typeNumber), rawText);
  }
}
