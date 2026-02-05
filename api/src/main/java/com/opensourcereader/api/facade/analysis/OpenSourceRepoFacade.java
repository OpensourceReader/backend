package com.opensourcereader.api.facade.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.service.hierarchy.InheritanceLinkService;
import com.opensourcereader.core.analysis.dto.RepositoryArtifact;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.service.RepositoryArtifactService;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoFacade {

  @Value("${opensource-reader.local-clone-path}")
  private String localClonePath;

  @Value("${opensource-reader.work-tree-name}")
  private String workingTreeDirName;

  private final RepositoryArtifactService repositoryArtifactService;
  private final OpenSourceRepoService opensourceRepoService;
  private final MethodCallGraphService methodCallGraphService;
  private final InheritanceLinkService inheritanceLinkService;

  @Transactional
  public OpenSourceRepoResponse createRepo(OpenSourceRepoCreateRequest request) {
    try (RepositoryArtifact artifact =
        repositoryArtifactService.create(
            request.openSourceUri(), request.reference(), localClonePath, workingTreeDirName)) {
      // 베스트는 여기 Type을 꺼내서 만들어준 다음에 -> 만튼 타입을 토대로, 아래에서 작업하는거임
      OpenSourceRepo openSourceRepo =
          opensourceRepoService.createRepo(request.openSourceUri(), artifact.typeStructures());
      inheritanceLinkService.resolve(openSourceRepo.getTypes(), artifact.typeStructures());
      methodCallGraphService.create(openSourceRepo.getTypes(), artifact.typeStructures());

      return OpenSourceRepoResponse.from(openSourceRepo);
    }
  }

  @Transactional
  public OpenSourceRepoResponse getRepoById(Long repoId) {
    OpenSourceRepo openSourceRepo = opensourceRepoService.getRepoById(repoId);
    return OpenSourceRepoResponse.from(openSourceRepo);
  }

  @Transactional
  public void deleteRepoById(Long repoId) {
    opensourceRepoService.deleteRepoById(repoId);
  }
}
