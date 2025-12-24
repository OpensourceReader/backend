package com.opensourcereader.core.analysis.entity;

import lombok.Getter;

@Getter
public enum ContentType {
  TREE("tree", "040000"),
  FILE("blob", "100644"),
  EXECUTABLE_FILE("blob", "100755"),
  OTHERS("NOT_EXISTS", null);

  private final String value;
  private final String typeNumber;

  ContentType(String value, String typeNumber) {
    this.value = value;
    this.typeNumber = typeNumber;
  }

  public static ContentType getContentTypeFromTypeNumber(String typeNumber) {
    for (ContentType contentType : ContentType.values()) {
      if (typeNumber.equals(contentType.typeNumber)) {
        return contentType;
      }
    }
    return OTHERS;
  }

  public boolean isSupported() {
    return this != OTHERS;
  }
}
