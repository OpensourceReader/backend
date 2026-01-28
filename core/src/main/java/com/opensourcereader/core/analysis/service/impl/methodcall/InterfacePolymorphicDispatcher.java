package com.opensourcereader.core.analysis.service.impl.methodcall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.entity.type.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InterfacePolymorphicDispatcher {

  private final CodeMethodRepository codeMethodRepository;

  public List<DeclaredMethod> dispatchImplementations(List<DeclaredType> interfaces) {
    Set<DeclaredMethod> result = new HashSet<>();
    for (DeclaredType interfaceType : interfaces) {
      result.addAll(dispatchImplementationByBfs(interfaceType));
    }

    return codeMethodRepository.saveAll(result);
  }

  private List<DeclaredMethod> dispatchImplementationByBfs(DeclaredType type) {
    List<DeclaredMethod> result = new ArrayList<>();
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
            declaredMethod.addOutgoingCall(nextDeclaredMethod);
            result.add(declaredMethod);
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
