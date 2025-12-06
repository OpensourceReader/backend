package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.OpensourceRepo;
import com.opensourcereader.core.analysis.entity.OpensourceRepoContent;
import com.opensourcereader.core.analysis.repository.OpensourceRepoRepository;
import com.opensourcereader.core.analysis.service.OpensourceRepoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicOpensourceRepoService implements OpensourceRepoService {

  private final RepoContentRawTextFetcher repoContentRawTextFetcher;
  private final OpensourceRepoRepository opensourceRepoRepository;

  @Override
  public OpensourceRepo create(GitTree gitTree) {
    OpensourceRepo opensourceRepo = new OpensourceRepo(gitTree.url(), "1.1");
    List<GitTreeFileInfo> gitTreeFileInfos = gitTree.fileInfos();

    for (GitTreeFileInfo fileInfo : gitTreeFileInfos) {
      String rawText = repoContentRawTextFetcher.fetchRepoContent(fileInfo.url());
      OpensourceRepoContent content = OpensourceRepoContent.of(fileInfo, rawText, opensourceRepo);
      opensourceRepo.addContent(content);
    }

    return opensourceRepoRepository.save(opensourceRepo);
  }

  @Override
  public OpensourceRepo get(Long repositoryId) {
    return null;
  }

  @Override
  public void delete(Long repositoryId) {
  }

}
