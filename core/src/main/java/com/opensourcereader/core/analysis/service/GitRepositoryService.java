package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

public interface GitRepositoryService {

  String saveToLocal(String openSourceUri, String localPath);

  List<GitTreeFileInfo> getFlatTree(String localPath, String reference);

  Repository createRepositoryBuilder(String localPath);

  String getRawText(ObjectId blobId, Repository repo);

}
