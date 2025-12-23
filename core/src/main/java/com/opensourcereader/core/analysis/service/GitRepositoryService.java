package com.opensourcereader.core.analysis.service;

import java.nio.file.Path;
import java.util.List;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

public interface GitRepositoryService {

  Path saveToLocal(String openSourceUri, String localPath);

  List<GitTreeFileInfo> getFlatTree(Path localPath, String reference);

  Repository createRepositoryBuilder(Path localPath);

  String getRawText(ObjectId blobId, Repository repo);
}
