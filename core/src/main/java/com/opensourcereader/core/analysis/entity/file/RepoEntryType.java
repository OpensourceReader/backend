package com.opensourcereader.core.analysis.entity.file;

import lombok.Getter;

@Getter
public enum RepoEntryType {
  TREE("tree", "040000"),
  FILE("blob", "100644"),
  EXECUTABLE_FILE("blob", "100755"),
  OTHERS("NOT_EXISTS", null);

  private final String value;
  private final String typeNumber;

  RepoEntryType(String value, String typeNumber) {
    this.value = value;
    this.typeNumber = typeNumber;
  }

  public static RepoEntryType from(String typeNumber) {
    for (RepoEntryType repoEntryType : RepoEntryType.values()) {
      if (typeNumber.equals(repoEntryType.typeNumber)) {
        return repoEntryType;
      }
    }
    return OTHERS;
  }

  public boolean isSupported() {
    return this != OTHERS;
  }
}
