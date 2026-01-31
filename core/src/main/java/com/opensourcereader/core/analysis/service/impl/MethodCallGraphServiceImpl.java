package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;
import com.opensourcereader.core.analysis.service.impl.detail.InheritanceMethodDispatcher;
import com.opensourcereader.core.analysis.service.impl.detail.InterfaceMethodDispatcher;
import com.opensourcereader.core.analysis.service.impl.detail.MethodCallResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MethodCallGraphServiceImpl implements MethodCallGraphService {

  private final InterfaceMethodDispatcher interfaceMethodDispatcher;
  private final InheritanceMethodDispatcher inheritanceMethodDispatcher;
  private final MethodCallResolver methodCallResolver;
  private final MethodRepository methodRepository;

  @Transactional
  @Override
  public void createMethodCallGraph(Long repoId, List<TypeStructureMeta> typeStructureMetas) {
    interfaceMethodDispatcher.connectInterfaceImplementations(repoId);
    inheritanceMethodDispatcher.connectInheritance(repoId);
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
