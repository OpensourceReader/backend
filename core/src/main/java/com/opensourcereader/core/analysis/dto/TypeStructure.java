package com.opensourcereader.core.analysis.dto;

import java.util.List;

import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;

public record TypeStructure(
    String path,
    RepoEntryType repoEntryType,
    String rawText,
    TypeInfo typeInfo,
    List<MethodStructure> methods) {

  public static TypeStructure of(
      OpenSourceFileInfo sourFile, TypeInfo typeInfo, List<MethodStructure> methods) {
    return new TypeStructure(
        sourFile.path(), sourFile.repoEntryType(), sourFile.rawText(), typeInfo, methods);
  }
}
