package com.opensourcereader.core.analysis.service.basic;

import com.opensourcereader.core.analysis.dto.GitTree;
import com.opensourcereader.core.analysis.dto.GitTreeFileInfo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LocalOpenSourceRepoService implements OpenSourceRepoService {

  private final OpenSourceRepoRepository opensourceRepoRepository;

  @Transactional
  @Override
  public OpenSourceRepo create(GitTree gitTree) {
    OpenSourceRepo opensourceRepo = new OpenSourceRepo(gitTree.cloneUrl());

    Repository repo = getRepo(gitTree);
    for (GitTreeFileInfo fileInfo : gitTree.fileInfos()) {
      try {
        ObjectLoader loader = repo.open(fileInfo.blobId(), Constants.OBJ_BLOB);
        byte[] bytes = loader.getBytes();
        String content = new String(bytes, StandardCharsets.UTF_8);
        opensourceRepo.addContent(OpenSourceRepoContent.of(fileInfo, content, opensourceRepo));
      } catch (IOException e) {
        throw new IllegalStateException("blob rawText 로드오류");
      }
    }

    return opensourceRepoRepository.save(opensourceRepo);
  }

  @Override
  public OpenSourceRepo get(Long repositoryId) {
    return opensourceRepoRepository.findById(repositoryId)
        .orElseThrow(() -> new IllegalArgumentException("Repo not found: " + repositoryId));
  }

  @Override
  public void delete(Long repositoryId) {
    opensourceRepoRepository.deleteById(repositoryId);
  }

  private Repository getRepo(GitTree gitTree) {
    try {
      return new FileRepositoryBuilder()
          .setGitDir(new File(gitTree.cloneUrl()))
          .build();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
