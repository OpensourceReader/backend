package com.opensourcereader.core.analysis.entity;

import lombok.Getter;

@Getter
public enum ContentType {
  TREE("tree", "100644"),
  SOURCE_CODE("blob", "040000"),
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
}
