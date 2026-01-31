package com.opensourcereader.core.analysis.service.impl.detail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.entity.Type;
import com.opensourcereader.core.analysis.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClassSuperDispatcher {

  private final TypeRepository typeRepository;
  private final MethodRepository methodRepository;

  public void dispatchSupers(Long repoId) {
    List<Type> classes = typeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);

    Set<Method> result = new HashSet<>();
    Set<Long> visited = new HashSet<>();
    for (Type classType : classes) {
      result.addAll(dispatchSuperRecursively(classType, visited));
    }
    methodRepository.saveAll(result);
  }

  private Set<Method> dispatchSuperRecursively(Type type, Set<Long> visited) {
    if (visited.contains(type.getId())) {
      return new HashSet<>();
    }
    visited.add(type.getId());
    Set<Method> result = new HashSet<>();
    if (type.getSuperType() != null) {
      result.addAll(dispatchSuperRecursively(type.getSuperType(), visited));
    }
    result.addAll(dispatchSuperMethod(type));
    return result;
  }

  private List<Method> dispatchSuperMethod(Type childType) {
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
        childType.addInheritedVirtualMethod(superMethod);
        typeRepository.save(childType);
      }
      superMethod.addOutgoingCall(inheritedOverride);
      result.add(superMethod);
    }
    return result;
  }
}
