package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Service;

@Service
public class GitWorktreeManager {

  public Path createWorktree(String bareRepoPath, String ref) {
    try {
      Path worktreeDir = Files.createTempDirectory("ossr-worktree-");

      new ProcessBuilder(
              "git", "--git-dir", bareRepoPath, "worktree", "add", worktreeDir.toString(), ref)
          .inheritIO()
          .start()
          .waitFor();

      return worktreeDir;
    } catch (Exception e) {
      throw new RuntimeException("worktree create failed", e);
    }
  }

  public void removeWorktree(String bareRepoPath, String worktreeDir) {
    try {
      new ProcessBuilder(
              "git", "--git-dir", bareRepoPath, "worktree", "remove", "--force", worktreeDir)
          .inheritIO()
          .start()
          .waitFor();
    } catch (Exception e) {
      throw new IllegalArgumentException();
    }
  }
}
