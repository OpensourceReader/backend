package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitBlobLoadException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitRepositoryOpenException;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoService implements OpenSourceRepoService {

  private final GitRepositoryService gitRepositoryService;
  private final OpenSourceRepoRepository opensourceRepoRepository;

  @Transactional
  @Override
  public OpenSourceRepo createRepo(String savedLocalPath, String cloneUrl, String repoReference) {
    OpenSourceRepo opensourceRepo = new OpenSourceRepo(cloneUrl);
    List<GitTreeFileInfo> flatTree = gitRepositoryService.getFlatTree(savedLocalPath,
        repoReference);

    Repository repo = gitRepositoryService.createRepositoryBuilder(savedLocalPath);
    for (GitTreeFileInfo fileInfo : flatTree) {
      String rawText = gitRepositoryService.getRawText(fileInfo.blobId(), repo);
      opensourceRepo.addContent(OpenSourceRepoContent.of(fileInfo, rawText, opensourceRepo));
    }

    return opensourceRepoRepository.save(opensourceRepo);
  }

  @Override
  public OpenSourceRepo getRepoById(Long repositoryId) {
    return opensourceRepoRepository.findById(repositoryId)
        .orElseThrow(OpenSourceRepoNotFoundException::new);
  }

  @Override
  public void deleteRepoById(Long repositoryId) {
    opensourceRepoRepository.deleteById(repositoryId);
  }

}
