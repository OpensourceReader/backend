package com.opensourcereader.core.analysis.entity;

import lombok.Getter;

@Getter
public enum ContentType {
  TREE("tree"),
  SOURCE_CODE("blob"),
  OTHERS("NOT_EXISTS");

  private final String value;

  ContentType(String value) {
    this.value = value;
  }

  public static ContentType getContentType(String fileInfoType) {
    for (ContentType contentType : ContentType.values()) {
      if (fileInfoType.equals(contentType.value)) {
        return contentType;
      }
    }
    return OTHERS;
  }

}
