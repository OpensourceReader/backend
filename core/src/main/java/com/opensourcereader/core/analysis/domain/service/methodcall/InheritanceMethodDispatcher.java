package com.opensourcereader.core.analysis.domain.service.methodcall;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.MethodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InheritanceMethodDispatcher {

  private final MethodRepository methodRepository;

  public void connectInheritance(List<Type> types) {
    List<Type> classTypes =
        types.stream().filter(type -> type.isSameTypeKind(TypeKind.CLASS)).toList();

    Set<String> visitedTypeNames = new HashSet<>();
    for (Type classType : classTypes) {
      traverseInheritanceHierarchy(classType, visitedTypeNames);
    }
  }

  private void traverseInheritanceHierarchy(Type type, Set<String> visited) {
    if (visited.contains(type.getTypeInternalName())) {
      return;
    }
    visited.add(type.getTypeInternalName());

    if (type.getSuperType() != null) {
      traverseInheritanceHierarchy(type.getSuperType(), visited);
    }
    connectMethodsWithSuperType(type);
  }

  private void connectMethodsWithSuperType(Type childType) {
    Type superType = childType.getSuperType();
    if (superType == null) {
      return;
    }
    Map<MethodSignature, Method> childMethods =
        childType.getMethods().stream()
            .collect(Collectors.toMap(Method::getMethodSignature, it -> it));
    for (Method superMethod : superType.getMethods()) {
      Method inheritedOverride =
          childMethods.computeIfAbsent(
              superMethod.getMethodSignature(),
              signature ->
                  methodRepository.save(Method.createVirtual(superType, superMethod, childType)));

      superMethod.addOutgoingCall(inheritedOverride);
    }
  }
}
