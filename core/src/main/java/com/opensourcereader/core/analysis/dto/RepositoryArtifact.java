package com.opensourcereader.core.analysis.dto;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record RepositoryArtifact(Path savedLocalRepoPath, List<TypeStructure> typeStructures)
    implements AutoCloseable {

  @Override
  public void close() {
    try {
      FileUtil.removeDirectory(savedLocalRepoPath);
    } catch (Exception e) {
      log.warn("임시 디렉토리 삭제 실패: {}", savedLocalRepoPath, e);
    }
  }
}
