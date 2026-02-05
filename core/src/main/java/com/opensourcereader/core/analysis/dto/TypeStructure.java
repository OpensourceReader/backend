package com.opensourcereader.core.analysis.dto;

import java.util.ArrayList;
import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;

public record TypeStructure(
    String path,
    RepoFileType repoFileType,
    String rawText,
    TypeInfo typeInfo,
    List<MethodStructure> methods) {

  public static TypeStructure of(
      OpenSourceFileInfo sourFile, TypeInfo typeInfo, List<MethodStructure> methods) {
    if (methods == null) {
      methods = new ArrayList<>();
    }

    return new TypeStructure(
        sourFile.path(), sourFile.repoFileType(), sourFile.rawText(), typeInfo, methods);
  }
}
