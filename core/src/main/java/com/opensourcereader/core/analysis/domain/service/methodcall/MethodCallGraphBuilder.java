package com.opensourcereader.core.analysis.domain.service.methodcall;

import java.util.List;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.dto.TypeStructure;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MethodCallGraphBuilder {

  private final InterfaceMethodDispatcher interfaceMethodDispatcher;
  private final InheritanceMethodDispatcher inheritanceMethodDispatcher;
  private final MethodCallResolver methodCallResolver;

  public void analyze(List<Type> types, List<TypeStructure> typeStructures) {
    interfaceMethodDispatcher.connectInterfaceImplementations(types);
    inheritanceMethodDispatcher.connectInheritance(types);
    methodCallResolver.create(types, typeStructures);
  }
}
