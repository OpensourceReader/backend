package com.opensourcereader.core.analysis.service.local;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.util.FileUtil;
import java.io.File;
import java.io.IOException;
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
public class GitRepoLocalManageService {

  public Repository saveLocalToDirectory(String opensourceUri, String localPath) {
    FileUtil.createDirectory(localPath);
    File localDirectory = new File(localPath);
    if (!(localDirectory.exists() || localDirectory.isFile())) {
      try (Git git = Git.cloneRepository()
          .setURI(opensourceUri)
          .setDirectory(localDirectory)
          .setBare(true)
          .call()) {
      } catch (GitAPIException e) {
        throw new IllegalStateException(e);
      }
    }

    try {
      return new FileRepositoryBuilder()
          .setGitDir(localDirectory)
          .build();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public GitTree getFlatTreeOfRepo(Repository repo, String reference) {
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
        flatTrees.add(new GitTreeFileInfo(path, contentType, null, objId));
      }

      return new GitTree(repo.getDirectory().toString(), flatTrees);
    } catch (IOException e) {
      throw new IllegalStateException("flatTree 추출 시 에러");
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
