package com.opensourcereader.core.analysis.testfixture;

import java.util.Arrays;
import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;

public final class TestRepoFixtures {

  private TestRepoFixtures() {}

  public static OpenSourceRepo saveRepo(
      OpenSourceRepoRepository openSourceRepoRepository,
      String cloneUrl,
      List<TypeStructure> typeStructures) {
    OpenSourceRepo repo = OpenSourceRepo.of(cloneUrl, typeStructures);
    openSourceRepoRepository.save(repo);
    return repo;
  }

  public static void linkInheritance(
      TypeRepository typeRepository,
      Long repoId,
      String childInternalName,
      String parentInternalName) {
    Type childType =
        typeRepository.findByRepoAndTypeInternalName(repoId, childInternalName).orElseThrow();

    Type parentType =
        typeRepository.findByRepoAndTypeInternalName(repoId, parentInternalName).orElseThrow();

    childType.updateRelations(parentType, List.of());
    typeRepository.save(childType);
  }

  public static List<Type> loadDeclaredTypes(
      TypeRepository typeRepository, Long repoId, String... typeInternalNames) {
    return Arrays.stream(typeInternalNames)
        .map(name -> typeRepository.findByRepoAndTypeInternalName(repoId, name).orElseThrow())
        .toList();
  }
}
