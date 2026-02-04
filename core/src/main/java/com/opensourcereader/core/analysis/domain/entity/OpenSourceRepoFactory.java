package com.opensourcereader.core.analysis.domain.entity;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.domain.entity.factory.ExternalTypeStructureFactory;
import com.opensourcereader.core.analysis.domain.service.hierarchy.InheritanceLinker;
import com.opensourcereader.core.analysis.dto.TypeStructure;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceRepoFactory {

  private final InheritanceLinker inheritanceLinker;
  private final ExternalTypeStructureFactory externalTypeStructureFactory;

  public OpenSourceRepo create(String cloneUri, List<TypeStructure> typeStructures) {
    OpenSourceRepo openSourceRepo =
        OpenSourceRepo.create(cloneUri, typeStructures, externalTypeStructureFactory);
    inheritanceLinker.resolve(openSourceRepo.getTypes(), typeStructures);
    return openSourceRepo;
  }
}
