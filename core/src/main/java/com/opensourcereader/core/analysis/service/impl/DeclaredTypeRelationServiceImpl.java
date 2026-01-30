package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.DeclaredType;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.service.DeclaredTypeRelationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeclaredTypeRelationServiceImpl implements DeclaredTypeRelationService {

  private final DeclaredTypeRepository declaredTypeRepository;
  private final OpenSourceRepoRepository openSourceRepoRepository;

  public List<DeclaredType> resolve(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    if (repoId == null || typeStructureMetas == null || typeStructureMetas.isEmpty()) {
      return List.of();
    }

    List<DeclaredType> declaredTypes = new ArrayList<>();
    for (TypeInfo typeInfo : getClassInfos(typeStructureMetas)) {
      DeclaredType declaredType =
          declaredTypeRepository
              .findByRepoAndTypeInternalName(repoId, typeInfo.internalName())
              .orElseThrow(IllegalArgumentException::new);
      declaredType.updateRelations(
          findOrCreateExternal(repoId, typeInfo.superName()), getInterfaceTypes(repoId, typeInfo));
      declaredTypes.add(declaredType);
    }

    return declaredTypeRepository.saveAll(declaredTypes);
  }

  private List<DeclaredType> getInterfaceTypes(Long repoId, TypeInfo typeInfo) {
    return typeInfo.interfaceNames().stream()
        .map(interfaceName -> this.findOrCreateExternal(repoId, interfaceName))
        .toList();
  }

  private DeclaredType findOrCreateExternal(Long repoId, String typeName) {
    if (typeName == null) {
      return null;
    }
    Optional<DeclaredType> declaredType =
        declaredTypeRepository.findByRepoAndTypeInternalName(repoId, typeName);
    if (declaredType.isPresent()) {
      return declaredType.get();
    }
    OpenSourceRepo repo =
        openSourceRepoRepository.findById(repoId).orElseThrow(IllegalArgumentException::new);
    repo.addExternalContent(typeName);
    OpenSourceRepo saveRepo = openSourceRepoRepository.save(repo);
    OpenSourceRepoContent content = saveRepo.getContents().get(saveRepo.getContents().size() - 1);
    return content.getDeclaredType();
  }

  private List<TypeInfo> getClassInfos(List<TypeStructureMeta> typeStructureMetas) {
    return typeStructureMetas.stream().map(TypeStructureMeta::typeInfo).toList();
  }
}
