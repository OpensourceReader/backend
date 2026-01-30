package com.opensourcereader.core.analysis.service.impl.methodcall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.entity.MethodSignature;
import com.opensourcereader.core.analysis.entity.DeclaredType;
import com.opensourcereader.core.analysis.entity.shared.TypeKind;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.MethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClassSuperDispatcher {

  private final DeclaredTypeRepository declaredTypeRepository;
  private final MethodRepository methodRepository;

  public void dispatchSupers(Long repoId) {
    List<DeclaredType> classes =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);

    Set<Method> result = new HashSet<>();
    Set<Long> visited = new HashSet<>();
    for (DeclaredType classType : classes) {
      result.addAll(dispatchSuperRecursively(classType, visited));
    }
    methodRepository.saveAll(result);
  }

  private Set<Method> dispatchSuperRecursively(DeclaredType type, Set<Long> visited) {
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

  private List<Method> dispatchSuperMethod(DeclaredType childType) {
    List<Method> result = new ArrayList<>();
    DeclaredType superType = childType.getSuperType();
    if (superType == null) {
      return new ArrayList<>();
    }
    Map<MethodSignature, Method> childMethods =
        childType.getMethods().stream()
            .collect(Collectors.toMap(Method::getMethodSignature, it -> it));
    for (Method superMethod : superType.getMethods()) {
      Method childMethodSameWithSuper = childMethods.get(superMethod.getMethodSignature());
      if (childMethodSameWithSuper == null) {
        childMethodSameWithSuper = getMethodSameWithSuper(childType, superMethod, superType);
        childType.addMethod(childMethodSameWithSuper);
        methodRepository.save(childMethodSameWithSuper); // 수정바람
      }
      superMethod.addOutgoingCall(childMethodSameWithSuper);
      result.add(superMethod);
    }
    return result;
  }

  private Method getMethodSameWithSuper(
      DeclaredType childType, Method superMethod, DeclaredType superType) {
    if (superType.isInternal()) {
      return Method.inheritedInternal(superMethod, childType);
    }
    return Method.inheritedExternal(superMethod, childType);
  }
}
