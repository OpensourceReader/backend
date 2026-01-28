package com.opensourcereader.core.analysis.service.impl.methodcall;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodPolymorphicDispatcher {

  private final DeclaredTypeRepository declaredTypeRepository;
  private final InterfacePolymorphicDispatcher interfacePolymorphicDispatcher;
  private final ClassSuperDispatcher classSuperDispatcher;

  @Transactional
  public void dispatch(Long repoId) {
    List<DeclaredType> interfaces =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.INTERFACE);
    interfacePolymorphicDispatcher.dispatchImplementations(interfaces);

    List<DeclaredType> classes =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);
    classSuperDispatcher.dispatchSupers(classes);
  }
}
