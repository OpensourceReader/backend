package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GitWorktreeManagerCli {

  public Path createWorktree(Path bareRepoPath, String ref, String workingTreeDirName) {
    try {
      Path worktreeDir = Files.createDirectories(bareRepoPath.resolve(workingTreeDirName));

      ExecResult r =
          exec(
              List.of(
                  "git",
                  "--git-dir",
                  bareRepoPath.toString(),
                  "worktree",
                  "add",
                  worktreeDir.toString(),
                  ref));

      if (r.exitCode != 0) {
        // 실패하면 생성해둔 디렉토리 정리 시도
        tryDeleteDirectory(worktreeDir);
        throw new IllegalStateException(
            "git worktree add failed (exit=" + r.exitCode + "):\n" + r.output);
      }

      return worktreeDir;
    } catch (Exception e) {
      throw new IllegalStateException("worktree create failed", e);
    }
  }

  public void removeWorktree(Path bareRepoPath, Path worktreeDir) {
    try {
      ExecResult r =
          exec(
              List.of(
                  "git",
                  "--git-dir",
                  bareRepoPath.toString(),
                  "worktree",
                  "remove",
                  "--force",
                  worktreeDir.toString()));

      if (r.exitCode != 0) {
        throw new IllegalStateException(
            "git worktree remove failed (exit=" + r.exitCode + "):\n" + r.output);
      }
    } catch (Exception e) {
      throw new IllegalStateException("worktree remove failed", e);
    }
  }

  private static ExecResult exec(List<String> command) throws IOException, InterruptedException {
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.redirectErrorStream(true); // stderr -> stdout 합치기

    Process p = pb.start();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    p.getInputStream().transferTo(baos);

    int exit = p.waitFor();
    String out = baos.toString(StandardCharsets.UTF_8);

    return new ExecResult(exit, out);
  }

  private static void tryDeleteDirectory(Path dir) {
    try {
      if (Files.exists(dir)) {
        Files.walk(dir)
            .sorted((a, b) -> b.getNameCount() - a.getNameCount())
            .forEach(
                path -> {
                  try {
                    Files.deleteIfExists(path);
                  } catch (IOException ignored) {
                  }
                });
      }
    } catch (IOException ignored) {
    }
  }

  private record ExecResult(int exitCode, String output) {}
}
