package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.RepositoryArtifact;

public interface RepositoryArtifactService {
  RepositoryArtifact create(
      String openSourceUri, String reference, String localClonePath, String workingTreeDirName);
}
