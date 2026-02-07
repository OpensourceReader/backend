package com.opensourcereader.core.analysis.testfixture;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.junit.jupiter.api.io.TempDir;

public final class JGitBareRepoFixture {

  private JGitBareRepoFixture() {}

  public static String createBareRepoUri(
      @TempDir Path tempDir, Map<String, String> sourceFiles, String branch) throws Exception {
    Path workDir = tempDir.resolve("work");
    Path bareDir = tempDir.resolve("test-org").resolve("sample-repo.git");

    Files.createDirectories(workDir);
    Files.createDirectories(bareDir);

    // 1) work repo 생성
    try (Git work = Git.init().setDirectory(workDir.toFile()).call()) {
      // 소스파일 생성
      for (Entry<String, String> e : sourceFiles.entrySet()) {
        Path p = workDir.resolve(e.getKey());
        Files.createDirectories(p.getParent());
        Files.writeString(p, e.getValue());
      }

      // git add .
      work.add().addFilepattern(".").call();

      // 커밋 (깃을 커밋시, author/committer)
      work.commit()
          .setMessage("init")
          .setAuthor("test", "test@example.com")
          .setCommitter("test", "test@example.com")
          .call();

      return getRemoteBareRepoUrl(branch, bareDir, work);
    }
  }

  // 2) work repo -> bare repo를 remote로 등록
  private static String getRemoteBareRepoUrl(String branch, Path bareDir, Git work)
      throws GitAPIException, URISyntaxException {
    try (Git bare = Git.init().setBare(true).setDirectory(bareDir.toFile()).call()) {
      // remote 등록 + push
      String bareUri = bareDir.toUri().toString();

      // git remote add origin
      work.remoteAdd().setName("origin").setUri(new URIish(bareUri)).call();

      // 현재 HEAD를 branch로 push
      work.push()
          .setRemote("origin")
          .setRefSpecs(new RefSpec(Constants.HEAD + ":refs/heads/" + branch))
          .call();

      return bareUri;
    }
  }
}
