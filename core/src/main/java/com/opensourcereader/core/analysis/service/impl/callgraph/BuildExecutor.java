package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Service
public class BuildExecutor {

  public void build(Path worktree) {
    if (Files.exists(worktree.resolve("gradlew"))) {
      run(worktree, "./gradlew", "classes", "-x", "test");
      return;
    }

    if (Files.exists(worktree.resolve("mvnw"))) {
      run(worktree, "./mvnw", "-q", "-DskipTests", "package");
      return;
    }

    if (Files.exists(worktree.resolve("build.gradle"))
        || Files.exists(worktree.resolve("build.gradle.kts"))) {
      run(worktree, "gradle", "classes", "-x", "test");
      return;
    }

    if (Files.exists(worktree.resolve("pom.xml"))) {
      run(worktree, "mvn", "-q", "-DskipTests", "package");
      return;
    }

    throw new IllegalStateException("No build file found");
  }

  private void run(Path dir, String... cmd) {
    try {
      new ProcessBuilder(cmd).directory(dir.toFile()).inheritIO().start().waitFor();
    } catch (Exception e) {
      throw new RuntimeException("build failed", e);
    }
  }
}
