package com.opensourcereader.core.analysis.service.impl.support.bytecode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class BuildArtifactCollector {

  public List<Path> collectClassFiles(Path worktree) {
    try {
      return Files.walk(worktree)
          .filter(p -> p.toString().endsWith(".class"))
          .filter(p -> !p.toString().contains("/buildSrc/"))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
