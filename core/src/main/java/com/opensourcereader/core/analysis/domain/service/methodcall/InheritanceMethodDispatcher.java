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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InheritanceMethodDispatcher {

  public void connectInheritance(List<Type> types) {
    List<Type> classTypes =
        types.stream().filter(type -> type.isSameTypeKind(TypeKind.CLASS)).toList();

    for (Type classType : classTypes) {
      traverseInheritanceHierarchy(classType, new HashSet<>());
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
      Method inheritedOverride = childMethods.get(superMethod.getMethodSignature());
      if (inheritedOverride == null) {
        childType.addChildVirtualMethod(superMethod);
      }
      superMethod.addOutgoingCall(inheritedOverride);
    }
  }
}
