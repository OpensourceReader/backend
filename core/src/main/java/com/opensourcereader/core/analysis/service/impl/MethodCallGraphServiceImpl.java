package com.opensourcereader.core.analysis.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.service.methodcall.InheritanceMethodDispatcher;
import com.opensourcereader.core.analysis.domain.service.methodcall.InterfaceMethodDispatcher;
import com.opensourcereader.core.analysis.domain.service.methodcall.MethodCallResolver;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.service.MethodCallGraphService;

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
    // [문제없음] 여기는 메서드타입, 다 저장됨, -> 근데 애도 지금 virtual을 만들 사례가 코드에 없어서 그런거임, 애도 추후에 문제가 될듯
    interfaceMethodDispatcher.connectInterfaceImplementations(types);

    // [문제 있음] 여기도 메서드가 저장이 안되는 요소들이 있음, 387개
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
