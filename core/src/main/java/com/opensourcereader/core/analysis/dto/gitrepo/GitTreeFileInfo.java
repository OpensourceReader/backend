package com.opensourcereader.core.analysis.dto.gitrepo;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.treewalk.TreeWalk;

public record GitTreeFileInfo(String path, String type, ObjectId blobId) {

  public static GitTreeFileInfo from(TreeWalk walk) {
    return new GitTreeFileInfo(
        walk.getPathString(), walk.getFileMode(0).toString(), walk.getObjectId(0));
  }
}
