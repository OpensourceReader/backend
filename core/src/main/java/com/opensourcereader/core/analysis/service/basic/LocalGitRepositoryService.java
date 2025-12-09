package com.opensourcereader.core.analysis.service.basic;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.ContentType;
import com.opensourcereader.core.analysis.exception.gitrepo.GitCloneFailedException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitBlobLoadException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitReferenceNotFoundException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitRepositoryOpenException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitTreeAccessException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitTreeParseException;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.util.FileUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalGitRepositoryService implements GitRepositoryService {

  @Override
  public String saveToLocal(String openSourceUri, String localDirectory) {
    Path createdLocalPath = createLocalPath(openSourceUri, localDirectory);

    File localPathFile = createdLocalPath.toFile();
    FileUtil.createDirectory(localPathFile);
    if (!(localPathFile.exists() || localPathFile.isFile())) {
      try (Git git =
          Git.cloneRepository()
              .setURI(openSourceUri)
              .setDirectory(localPathFile)
              .setBare(true)
              .call()) {
      } catch (GitAPIException e) {
        throw new GitCloneFailedException().addDetail("cause", e.getCause());
      }
    }

    return createdLocalPath.toString();
  }

  @Override
  public List<GitTreeFileInfo> getFlatTree(String localPath, String reference) {
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
        if (contentType.equals(ContentType.OTHERS)) {
          continue;
        }
        flatTrees.add(new GitTreeFileInfo(path, contentType, objId));
      }

      return flatTrees;
    } catch (IOException e) {
      throw new LocalGitTreeParseException().addDetail("cause", e.getCause());
    }
  }

  @Override
  public String getRawText(ObjectId blobId, Repository repo) {
    try {
      ObjectLoader loader = repo.open(blobId, Constants.OBJ_BLOB);
      return new String(loader.getBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new LocalGitBlobLoadException().addDetail("cause", e.getCause());
    }
  }

  @Override
  public Repository createRepositoryBuilder(String localPath) {
    try {
      return new FileRepositoryBuilder().setGitDir(new File(localPath)).build();
    } catch (IOException e) {
      throw new LocalGitRepositoryOpenException().addDetail("cause", e.getCause());
    }
  }

  private Path createLocalPath(String openSourceUri, String localDirectory) {
    Path moduleDir = Paths.get("").toAbsolutePath();
    Path projectDir = moduleDir.getParent();

    String[] tokens = openSourceUri.split("/");
    String owner = tokens[tokens.length - 2];
    String repo = tokens[tokens.length - 1];

    return projectDir.resolve(localDirectory).resolve(owner).resolve(repo);
  }

  private RevTree getTree(Repository repo, ObjectId commitId) {
    try {
      RevWalk revWalk = new RevWalk(repo);
      RevCommit commit = revWalk.parseCommit(commitId);
      return commit.getTree();
    } catch (IOException e) {
      throw new LocalGitTreeAccessException().addDetail("cause", e.getCause());
    }
  }

  private ObjectId getCommitId(Repository repo, String reference) {
    try {
      return repo.resolve(reference);
    } catch (IOException e) {
      throw new LocalGitReferenceNotFoundException().addDetail("cause", e.getCause());
    }
  }
}
