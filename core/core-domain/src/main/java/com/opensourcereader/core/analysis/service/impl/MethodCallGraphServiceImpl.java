package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;
import com.opensourcereader.core.analysis.service.impl.methodCall.InheritanceMethodDispatcher;
import com.opensourcereader.core.analysis.service.impl.methodCall.InterfaceMethodDispatcher;
import com.opensourcereader.core.analysis.service.impl.methodCall.MethodCallResolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MethodCallGraphServiceImpl implements MethodCallGraphService {

  private final MethodRepository methodRepository;
  private final InterfaceMethodDispatcher interfaceMethodDispatcher;
  private final InheritanceMethodDispatcher inheritanceMethodDispatcher;
  private final MethodCallResolver methodCallResolver;

  @Override
  public void create(List<Type> types, List<TypeStructure> typeStructures) {
    interfaceMethodDispatcher.connectInterfaceImplementations(types);
    inheritanceMethodDispatcher.connectInheritance(types);
    methodCallResolver.create(types, typeStructures);
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
