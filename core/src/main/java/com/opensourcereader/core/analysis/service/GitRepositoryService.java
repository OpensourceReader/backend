package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.dto.GitTree;

public interface GitRepositoryService {

  String saveToLocal(String openSourceUri, String localPath);

  GitTree getFlatTree(String localPath, String reference);

}
