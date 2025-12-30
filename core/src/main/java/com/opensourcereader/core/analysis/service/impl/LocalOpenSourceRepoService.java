package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import com.opensourcereader.core.analysis.service.impl.factory.OpenSourceContentFactory;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoService implements OpenSourceRepoService {

  private final OpenSourceContentFactory openSourceContentFactory;
  private final OpenSourceRepoRepository opensourceRepoRepository;

  @Transactional
  @Override
  public OpenSourceRepo createRepo(
      String cloneUri,
      List<OpenSourceFileInfo> sourFileInfos,
      List<ClassStructure> classStructures) {
    validateAlreadyExist(cloneUri);
    OpenSourceRepo opensourceRepo = new OpenSourceRepo(cloneUri);
    List<OpenSourceRepoContent> openSourceRepoContents =
        openSourceContentFactory.create(sourFileInfos, classStructures, opensourceRepo);
    opensourceRepo.addAllContents(openSourceRepoContents);

    return opensourceRepoRepository.save(opensourceRepo);
  }

  @Override
  public OpenSourceRepo getRepoById(Long repositoryId) {
    return opensourceRepoRepository
        .findById(repositoryId)
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
