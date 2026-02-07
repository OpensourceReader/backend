package com.opensourcereader.core.analysis.infra.dto;

import java.util.Set;

import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;

public record OpenSourceFileInfo(String path, RepoFileType repoFileType, String rawText) {

  public static OpenSourceFileInfo of(String path, String typeNumber, String rawText) {
    return new OpenSourceFileInfo(path, RepoFileType.from(typeNumber), rawText);
  }

  public boolean isSafeText() {
    return isSupportedExtension() && rawText != null && rawText.indexOf('\0') < 0;
  }

  private static final Set<String> TEXT_EXTENSIONS =
      Set.of("java", "kt", "js", "ts", "py", "md", "xml", "yml", "yaml", "gradle");

  private boolean isSupportedExtension() {
    int dot = path.lastIndexOf(".");
    if (dot == -1) {
      return false;
    }

    String ext = path.substring(dot + 1).toLowerCase();
    return TEXT_EXTENSIONS.contains(ext);
  }
}
