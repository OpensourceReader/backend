package com.opensourcereader.api.facade.analysis;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opensourcereader.api.dto.OpenSourceRepoCreateRequest;
import com.opensourcereader.api.dto.OpenSourceRepoResponse;
import com.opensourcereader.core.analysis.dto.RepositoryArtifact;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.service.RepositoryArtifactService;
import com.opensourcereader.core.analysis.service.TypeHierarchyService;
import com.opensourcereader.core.analysis.util.FileUtil;
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
  private final TypeHierarchyService typeHierarchyService;
  private final MethodCallGraphService methodCallGraphService;

  @Transactional
  public OpenSourceRepoResponse createRepo(OpenSourceRepoCreateRequest request) {
    RepositoryArtifact artifact =
        repositoryArtifactService.create(
            request.openSourceUri(), request.reference(), localClonePath, workingTreeDirName);
    OpenSourceRepo openSourceRepo =
        opensourceRepoService.createRepo(request.openSourceUri(), artifact.typeStructures());

    List<TypeStructureMeta> typeStructureMetas = TypeStructureMeta.from(artifact.typeStructures());
    typeHierarchyService.resolve(openSourceRepo.getId(), typeStructureMetas);
    methodCallGraphService.createMethodCallGraph(openSourceRepo.getId(), typeStructureMetas);

    FileUtil.removeDirectory(artifact.savedLocalRepoPath());
    return OpenSourceRepoResponse.from(openSourceRepo);
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
