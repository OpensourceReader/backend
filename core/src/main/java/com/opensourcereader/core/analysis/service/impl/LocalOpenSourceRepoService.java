package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.GitRepositoryService;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.user.entity.User;
import jakarta.transaction.Transactional;
import org.eclipse.jgit.lib.Repository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoService implements OpenSourceRepoService {

  private final GitRepositoryService gitRepositoryService;
  private final OpenSourceRepoRepository opensourceRepoRepository;

  @Override
  public OpenSourceRepo getOrCreateRepoInDB(User owner, String title) {
    OpenSourceRepo openSourceRepo =
        opensourceRepoRepository
            .findByOwnerAndTitle(owner, title)
            .orElseGet(() -> new OpenSourceRepo(owner, title));
    return opensourceRepoRepository.save(openSourceRepo);
  }

  @Transactional
  @Override
  public OpenSourceRepo createRepo(String savedLocalPath, String cloneUrl, String repoReference) {
    validateAlreadyExist(cloneUrl);

    OpenSourceRepo opensourceRepo = new OpenSourceRepo(cloneUrl);
    List<GitTreeFileInfo> flatTree =
        gitRepositoryService.getFlatTree(savedLocalPath, repoReference);

    Repository repo = gitRepositoryService.createRepositoryBuilder(savedLocalPath);
    for (GitTreeFileInfo fileInfo : flatTree) {
      String rawText = gitRepositoryService.getRawText(fileInfo.blobId(), repo);
      opensourceRepo.addContent(OpenSourceRepoContent.of(fileInfo, rawText, opensourceRepo));
    }

    return opensourceRepoRepository.save(opensourceRepo);
  }

  @Override
  public OpenSourceRepo getRepoById(Long repositoryId) {
    return opensourceRepoRepository
        .findById(repositoryId)
        .orElseThrow(OpenSourceRepoNotFoundException::new);
  }

  @Override
  public OpenSourceRepo getRepoByOwnerNameAndTitle(String ownerName, String title) {
    return opensourceRepoRepository
        .findByOwnerLoginNameAndTitle(ownerName, title)
        .orElseThrow(OpenSourceRepoNotFoundException::new);
  }

  @Override
  public void deleteRepoById(Long repositoryId) {
    opensourceRepoRepository.deleteById(repositoryId);
  }

  private void validateAlreadyExist(String cloneUrl) {
    if (opensourceRepoRepository.existsByCloneUrl(cloneUrl)) {
      throw new OpenSourceRepoAlreadyExistException();
    }
  }
}
