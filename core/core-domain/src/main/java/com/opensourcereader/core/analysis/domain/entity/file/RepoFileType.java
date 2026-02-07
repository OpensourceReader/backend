package com.opensourcereader.core.analysis.domain.entity.file;

import lombok.Getter;

@Getter
public enum RepoFileType {
  TREE("tree", "040000"),
  FILE("blob", "100644"),
  EXECUTABLE_FILE("blob", "100755"),
  OTHERS("NOT_EXISTS", null);

  private final String value;
  private final String typeNumber;

  RepoFileType(String value, String typeNumber) {
    this.value = value;
    this.typeNumber = typeNumber;
  }

  public static RepoFileType from(String typeNumber) {
    for (RepoFileType repoFileType : RepoFileType.values()) {
      if (typeNumber.equals(repoFileType.typeNumber)) {
        return repoFileType;
      }
    }
    return OTHERS;
  }

  public boolean isSupported() {
    return this != OTHERS;
  }
}
