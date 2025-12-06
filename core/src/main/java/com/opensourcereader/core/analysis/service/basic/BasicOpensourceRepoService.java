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
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class BasicOpensourceRepoService implements OpensourceRepoService {

  private final RestTemplate restTemplate;
  private final OpensourceRepoRepository opensourceRepoRepository;

  // 1. rawText를 가져오는 코드
  // 2. 메모리부담 개선 -> 한 300 되는거, 한번에 리스트로 다 저장할거야? 이건 아니잖아, 램터져
  @Override
  public OpensourceRepo create(GitTree gitTree) {
    OpensourceRepo opensourceRepo = new OpensourceRepo(gitTree.url(), "1.1");
    List<GitTreeFileInfo> gitTreeFileInfos = gitTree.fileInfos();

    for (GitTreeFileInfo fileInfo : gitTreeFileInfos) {
      String rawText = restTemplate.getForObject(fileInfo.url(), String.class);
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
