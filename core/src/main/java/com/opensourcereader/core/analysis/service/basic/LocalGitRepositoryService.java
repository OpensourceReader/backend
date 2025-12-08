package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalGitRepositoryService implements GitRepositoryService {

  @Override
  public String saveToLocal(String openSourceUri, String localDirectory) {
    Path createdLocalPath = createLocalPath(openSourceUri, localDirectory);

    File localPathFile = createdLocalPath.toFile();
    FileUtil.createDirectory(localPathFile); // 여기서 걸리긴하는데;;
    if (!(localPathFile.exists() || localPathFile.isFile())) {
      try (Git git = Git.cloneRepository()
          .setURI(openSourceUri)
          .setDirectory(localPathFile)
          .setBare(true)
          .call()) {
      } catch (GitAPIException e) {
        throw new IllegalStateException(e);
      }
    }

    return createdLocalPath.toString();
  }

  @Override
  public GitTree getFlatTree(String localPath, String reference) {
    Repository repo = createRepositoryBuilder(localPath);
    ObjectId commitId = getCommitId(repo, reference);
    RevTree tree = getTree(repo, commitId);

    List<GitTreeFileInfo> flatTrees = new ArrayList<>();
    try {
      TreeWalk walk = new TreeWalk(repo);
      walk.addTree(tree);
      walk.setRecursive(true);

      while (walk.next()) {
        String path = walk.getPathString();
        FileMode type = walk.getFileMode(0);
        ObjectId objId = walk.getObjectId(0);

        ContentType contentType = ContentType.getContentTypeFromTypeNumber(type.toString());
        if (!(contentType.equals(ContentType.TREE) ||
            contentType.equals(ContentType.SOURCE_CODE))
        ) {
          continue;
        }
        flatTrees.add(new GitTreeFileInfo(path, contentType, objId));
      }

      return new GitTree(repo.getDirectory().toString(), flatTrees);
    } catch (IOException e) {
      throw new IllegalStateException("flatTree 추출 시 에러");
    }
  }

  private Path createLocalPath(String openSourceUri, String localDirectory) {
    Path moduleDir = Paths.get("").toAbsolutePath();
    Path projectDir = moduleDir.getParent();

    String[] tokens = openSourceUri.split("/");
    String owner = tokens[tokens.length - 2];
    String repo = tokens[tokens.length - 1];

    return projectDir
        .resolve(localDirectory)
        .resolve(owner)
        .resolve(repo);
  }

  private Repository createRepositoryBuilder(String localPath) {
    try {
      return new FileRepositoryBuilder()
          .setGitDir(new File(localPath))
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("JGit 레포지토리 생성 실패");
    }
  }

  private RevTree getTree(Repository repo, ObjectId commitId) {
    try {
      RevWalk revWalk = new RevWalk(repo);
      RevCommit commit = revWalk.parseCommit(commitId);
      return commit.getTree();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private ObjectId getCommitId(Repository repo, String reference) {
    try {
      return repo.resolve(reference);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
