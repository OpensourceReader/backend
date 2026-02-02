package com.opensourcereader.core.analysis.domain.service;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InheritanceMethodDispatcher {

  private final TypeRepository typeRepository;
  private final MethodRepository methodRepository;

  public void connectInheritance(Long repoId) {
    List<Type> types = typeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);

    Set<Method> result = new HashSet<>();
    Set<Long> visited = new HashSet<>();
    for (Type type : types) {
      result.addAll(traverseInheritanceHierarchy(type, visited));
    }
    methodRepository.saveAll(result);
  }

  private Set<Method> traverseInheritanceHierarchy(Type type, Set<Long> visited) {
    if (visited.contains(type.getId())) {
      return new HashSet<>();
    }
    visited.add(type.getId());
    Set<Method> result = new HashSet<>();
    if (type.getSuperType() != null) {
      result.addAll(traverseInheritanceHierarchy(type.getSuperType(), visited));
    }
    result.addAll(connectMethodsWithSuperType(type));
    return result;
  }

  private List<Method> connectMethodsWithSuperType(Type childType) {
    List<Method> result = new ArrayList<>();
    Type superType = childType.getSuperType();
    if (superType == null) {
      return new ArrayList<>();
    }
    Map<MethodSignature, Method> childMethods =
        childType.getMethods().stream()
            .collect(Collectors.toMap(Method::getMethodSignature, it -> it));
    for (Method superMethod : superType.getMethods()) {
      Method inheritedOverride = childMethods.get(superMethod.getMethodSignature());
      if (inheritedOverride == null) {
        childType.addChildVirtualMethod(superMethod);
        typeRepository.save(childType);
      }
      superMethod.addOutgoingCall(inheritedOverride);
      result.add(superMethod);
    }
    return result;
  }
}
