package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.entity.OpensourceRepository;
import com.opensourcereader.core.analysis.service.OpensourceRepositoryService;
import org.springframework.stereotype.Service;

@Service
public class BasicOpensourceRepositoryService implements OpensourceRepositoryService {

  @Override
  public OpensourceRepository create(String treeApiUrl) {
    return null;
  }

  @Override
  public OpensourceRepository get(Long repositoryId) {
    return null;
  }

  @Override
  public void delete(Long repositoryId) {

  }

}
