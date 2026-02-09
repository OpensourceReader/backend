package com.opensourcereader.core.analysis.infra.bytecode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.infra.dto.ClassBytecode;
import com.opensourcereader.core.analysis.util.FileUtil;

@Component
public class BuildArtifactCollector {

  public List<ClassBytecode> collectClassFiles(Path worktree) {
    try {
      return Files.walk(worktree)
          .filter(path -> path.toString().endsWith(".class"))
          .filter(path -> !path.toString().contains("/buildSrc/"))
          .map(path -> new ClassBytecode(path, FileUtil.readAllBytes(path)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
