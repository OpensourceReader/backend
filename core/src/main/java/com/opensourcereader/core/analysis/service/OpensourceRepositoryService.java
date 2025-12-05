package com.opensourcereader.core.analysis.service;

import com.opensourcereader.core.analysis.entity.OpensourceRepository;

public interface OpensourceRepositoryService {

  OpensourceRepository create(String treeApiUrl);

  OpensourceRepository get(Long repositoryId);

  void delete(Long repositoryId);

}
