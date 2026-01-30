package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;
import com.opensourcereader.core.analysis.service.impl.methodcall.ClassSuperDispatcher;
import com.opensourcereader.core.analysis.service.impl.methodcall.InterfacePolymorphicDispatcher;
import com.opensourcereader.core.analysis.service.impl.methodcall.MethodCallResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodCallGraphServiceImpl implements CodeMethodCallGraphService {

  private final InterfacePolymorphicDispatcher interfacePolymorphicDispatcher;
  private final ClassSuperDispatcher classSuperDispatcher;
  private final MethodCallResolver methodCallResolver;
  private final CodeMethodRepository codeMethodRepository;

  @Transactional
  @Override
  public void createMethodCallGraph(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    interfacePolymorphicDispatcher.dispatchImplementations(repoId);
    classSuperDispatcher.dispatchSupers(repoId);
    methodCallResolver.create(repoId, typeStructureMetas);
  }

  @Transactional
  @Override
  public DeclaredMethod getCodeMethodById(Long codeMethodId) {
    codeMethodRepository
        .findWithOutgoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);

    return codeMethodRepository
        .findWithIngoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);
  }
}
