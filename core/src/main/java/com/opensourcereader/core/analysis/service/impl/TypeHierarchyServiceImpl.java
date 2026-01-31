package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.entity.Type;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;
import com.opensourcereader.core.analysis.service.TypeHierarchyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TypeHierarchyServiceImpl implements TypeHierarchyService {

  private final TypeRepository typeRepository;
  private final OpenSourceRepoRepository openSourceRepoRepository;

  public List<Type> resolve(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    if (repoId == null || typeStructureMetas == null || typeStructureMetas.isEmpty()) {
      return List.of();
    }

    List<Type> types = new ArrayList<>();
    for (TypeInfo typeInfo : getClassInfos(typeStructureMetas)) {
      Type type =
          typeRepository
              .findByRepoAndTypeInternalName(repoId, typeInfo.internalName())
              .orElseThrow(IllegalArgumentException::new);
      type.updateRelations(
          findOrCreateExternal(repoId, typeInfo.superName()), getInterfaceTypes(repoId, typeInfo));
      types.add(type);
    }

    return typeRepository.saveAll(types);
  }

  private List<Type> getInterfaceTypes(Long repoId, TypeInfo typeInfo) {
    return typeInfo.interfaceNames().stream()
        .map(interfaceName -> this.findOrCreateExternal(repoId, interfaceName))
        .toList();
  }

  private Type findOrCreateExternal(Long repoId, String typeName) {
    if (typeName == null) {
      return null;
    }
    Optional<Type> declaredType = typeRepository.findByRepoAndTypeInternalName(repoId, typeName);
    if (declaredType.isPresent()) {
      return declaredType.get();
    }
    OpenSourceRepo repo =
        openSourceRepoRepository.findById(repoId).orElseThrow(IllegalArgumentException::new);
    repo.addExternalContent(typeName);
    OpenSourceRepo saveRepo = openSourceRepoRepository.save(repo);
    OpenSourceRepoContent content = saveRepo.getContents().get(saveRepo.getContents().size() - 1);
    return content.getType();
  }

  private List<TypeInfo> getClassInfos(List<TypeStructureMeta> typeStructureMetas) {
    return typeStructureMetas.stream().map(TypeStructureMeta::typeInfo).toList();
  }
}
