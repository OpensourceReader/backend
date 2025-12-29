package com.opensourcereader.core.analysis.service.impl;

import static com.opensourcereader.core.analysis.entity.ContentType.getContentTypeFromTypeNumber;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoAlreadyExistException;
import com.opensourcereader.core.analysis.exception.opensourcerepo.OpenSourceRepoNotFoundException;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoService implements OpenSourceRepoService {

  private final OpenSourceCodeMethodExtractor openSourceCodeMethodExtractor;
  private final OpenSourceRepoRepository opensourceRepoRepository;

  @Transactional
  @Override
  public OpenSourceRepo createRepo(
      String cloneUri,
      List<OpenSourceFileInfo> sourFileInfos,
      List<ClassStructure> classStructures) {
    validateAlreadyExist(cloneUri);

    OpenSourceRepo opensourceRepo = new OpenSourceRepo(cloneUri);
    Map<String, ClassStructure> structures = getByClassName(classStructures);
    List<OpenSourceRepoContent> openSourceRepoContents =
        sourFileInfos.stream()
            .filter(
                sourFileInfo ->
                    getContentTypeFromTypeNumber(sourFileInfo.typeNumber()).isSupported())
            .map(
                sourFileInfo -> {
                  List<CodeMethodExtractResult> methods =
                      openSourceCodeMethodExtractor.extract(sourFileInfo, structures);
                  return OpenSourceRepoContent.of(sourFileInfo, methods, opensourceRepo);
                })
            .toList();
    opensourceRepo.addAllContents(openSourceRepoContents);

    return opensourceRepoRepository.save(opensourceRepo);
  }

  private Map<String, ClassStructure> getByClassName(List<ClassStructure> classStructures) {
    return classStructures.stream()
        .collect(Collectors.toMap(cs -> cs.classInfo().className(), cs -> cs));
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
