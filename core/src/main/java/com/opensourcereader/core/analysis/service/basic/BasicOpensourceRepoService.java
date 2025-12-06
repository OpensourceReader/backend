package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import com.opensourcereader.core.analysis.repository.OpensourceRepoContentRepository;
import com.opensourcereader.core.analysis.service.OpensourceRepoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicOpensourceRepoService implements OpensourceRepoService {

  private final OpensourceRepoContentRepository opensourceRepoContentRepository;

  @Override
  public OpensourceRepo create(String treeApiUrl) {
    return null;
  }

  @Override
  public OpensourceRepo get(Long repositoryId) {
    return null;
  }

  @Override
  public void delete(Long repositoryId) {
  }

}
