package com.opensourcereader.core.analysis.entity;

public enum ContentType {
  SOURCE_CODE,
  OTHERS;

  public static ContentType getContentType(String fileInfoType) {
    if (fileInfoType.equals("blob")) {
      return ContentType.SOURCE_CODE;
    }
    return ContentType.OTHERS;
  }

}
