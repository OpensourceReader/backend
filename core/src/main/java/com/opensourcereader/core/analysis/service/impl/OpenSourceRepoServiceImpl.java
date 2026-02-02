package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenSourceRepoServiceImpl implements OpenSourceRepoService {

  private final OpenSourceRepoRepository opensourceRepoRepository;

  @Transactional
  @Override
  public OpenSourceRepo createRepo(String cloneUri, List<TypeStructure> typeStructures) {
    validateAlreadyExist(cloneUri);
    OpenSourceRepo openSourceRepo = OpenSourceRepo.of(cloneUri, typeStructures);
    return opensourceRepoRepository.save(openSourceRepo);
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
