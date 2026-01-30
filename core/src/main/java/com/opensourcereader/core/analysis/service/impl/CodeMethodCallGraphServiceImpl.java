package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;
import com.opensourcereader.core.analysis.service.impl.methodcall.ClassSuperDispatcher;
import com.opensourcereader.core.analysis.service.impl.methodcall.InterfaceImplementationLinker;
import com.opensourcereader.core.analysis.service.impl.methodcall.MethodCallResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodCallGraphServiceImpl implements CodeMethodCallGraphService {

  private final InterfaceImplementationLinker interfaceImplementationLinker;
  private final ClassSuperDispatcher classSuperDispatcher;
  private final MethodCallResolver methodCallResolver;
  private final MethodRepository methodRepository;

  @Transactional
  @Override
  public void createMethodCallGraph(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    interfaceImplementationLinker.linkAllInterfaceImplementations(repoId);
    classSuperDispatcher.dispatchSupers(repoId);
    methodCallResolver.create(repoId, typeStructureMetas);
  }

  @Transactional
  @Override
  public Method getCodeMethodById(Long codeMethodId) {
    methodRepository
        .findWithOutgoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);

    return methodRepository
        .findWithIngoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);
  }
}
