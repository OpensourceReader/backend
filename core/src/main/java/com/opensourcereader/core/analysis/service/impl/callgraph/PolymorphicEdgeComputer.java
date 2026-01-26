package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.method.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.entity.type.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.entity.type.TypeKind;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolymorphicEdgeComputer {

  public List<CodeMethodCallEdge> compute(
      List<DeclaredType> interfaceTypes, List<DeclaredType> classTypes) {
    Set<CodeMethodCallEdge> result = new HashSet<>();
    for (DeclaredType interfaceType : interfaceTypes) {
      result.addAll(dispatchImplementationByBfs(interfaceType));
    }
    for (DeclaredType classType : classTypes) {
      result.addAll(dispatchSuperMethod(classType));
    }
    return result.stream().toList();
  }

  private List<CodeMethodCallEdge> dispatchSuperMethod(DeclaredType childType) {
    List<CodeMethodCallEdge> result = new ArrayList<>();
    DeclaredType superType = childType.getSuperType();
    if (superType == null) {
      return new ArrayList<>();
    }
    Map<CodeMethodSignature, DeclaredMethod> childMethods =
        childType.getDeclaredMethods().stream()
            .collect(Collectors.toMap(DeclaredMethod::getMethodSignature, it -> it));
    for (DeclaredMethod superMethod : superType.getDeclaredMethods()) {
      DeclaredMethod childMethodSameWithSuper = childMethods.get(superMethod.getMethodSignature());
      if (childMethodSameWithSuper == null) {
        childMethodSameWithSuper =
            DeclaredMethod.internalInheritanceDeclared(superMethod, childType);
        childType.updateMethod(childMethodSameWithSuper);
      }

      result.add(CodeMethodCallEdge.of(superMethod, childMethodSameWithSuper));
    }

    return result;
  }

  private List<CodeMethodCallEdge> dispatchImplementationByBfs(DeclaredType type) {
    List<CodeMethodCallEdge> result = new ArrayList<>();
    Queue<DeclaredType> queue = new LinkedList<>();
    queue.add(type);

    while (!queue.isEmpty()) {
      int n = queue.size();
      for (int i = 0; i < n; i++) {
        DeclaredType polledType = queue.poll();
        for (DeclaredTypeImplementEdge implementEdge : polledType.getImplementations()) {
          DeclaredType newDeclaredType = implementEdge.getType();
          Map<CodeMethodSignature, DeclaredMethod> collect =
              newDeclaredType.getDeclaredMethods().stream()
                  .collect(Collectors.toMap(DeclaredMethod::getMethodSignature, it -> it));

          for (DeclaredMethod declaredMethod : polledType.getDeclaredMethods()) {
            DeclaredMethod nextDeclaredMethod =
                collect.getOrDefault(
                    declaredMethod.getMethodSignature(),
                    DeclaredMethod.internalInheritanceDeclared(declaredMethod, newDeclaredType));
            result.add(CodeMethodCallEdge.of(declaredMethod, nextDeclaredMethod));
          }
          if (newDeclaredType.getTypeKind().equals(TypeKind.INTERFACE)) {
            queue.add(newDeclaredType);
          }
        }
      }
    }

    return result;
  }
}
