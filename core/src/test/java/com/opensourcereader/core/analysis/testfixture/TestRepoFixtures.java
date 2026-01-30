package com.opensourcereader.core.analysis.testfixture;

import java.util.Arrays;
import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;

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
      DeclaredTypeRepository declaredTypeRepository,
      Long repoId,
      String childInternalName,
      String parentInternalName) {
    DeclaredType childType =
        declaredTypeRepository
            .findByRepoAndTypeInternalName(repoId, childInternalName)
            .orElseThrow();

    DeclaredType parentType =
        declaredTypeRepository
            .findByRepoAndTypeInternalName(repoId, parentInternalName)
            .orElseThrow();

    childType.updateRelations(parentType, List.of());
    declaredTypeRepository.save(childType);
  }

  public static List<DeclaredType> loadDeclaredTypes(
      DeclaredTypeRepository declaredTypeRepository, Long repoId, String... typeInternalNames) {
    return Arrays.stream(typeInternalNames)
        .map(
            name ->
                declaredTypeRepository.findByRepoAndTypeInternalName(repoId, name).orElseThrow())
        .toList();
  }
}
