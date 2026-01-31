package com.opensourcereader.core.analysis.service.impl.detail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.entity.Type;
import com.opensourcereader.core.analysis.entity.TypeImplementation;
import com.opensourcereader.core.analysis.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.MethodRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InterfaceImplementationLinker {

  private final DeclaredTypeGraphValidator declaredTypeGraphValidator;
  private final MethodRepository methodRepository;
  private final DeclaredTypeRepository declaredTypeRepository;

  public List<Method> linkAllInterfaceImplementations(Long repoId) {
    List<Type> interfaces =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.INTERFACE);

    Set<Method> result = new HashSet<>();
    for (Type interfaceType : interfaces) {
      declaredTypeGraphValidator.validateAcyclic(interfaceType);
      result.addAll(linkInterfaceImplementations(interfaceType));
    }

    return methodRepository.saveAll(result);
  }

  private List<Method> linkInterfaceImplementations(Type interfaceType) {
    List<Method> result = new ArrayList<>();
    Queue<Type> queue = new LinkedList<>();
    queue.add(interfaceType);

    while (!queue.isEmpty()) {
      int n = queue.size();
      for (int i = 0; i < n; i++) {
        Type nowInterfaceType = queue.poll();
        for (TypeImplementation implEdge : nowInterfaceType.getImplementations()) {
          Type implType = implEdge.getImplementedType();
          Map<MethodSignature, Method> implTypeMethods =
              getImplementationTypeDeclaredMethods(implType);
          for (Method nowInterfaceTypeMethod : nowInterfaceType.getMethods()) {
            Method implMethod =
                getImplMethod(nowInterfaceTypeMethod, nowInterfaceType, implTypeMethods, implType);
            nowInterfaceTypeMethod.addOutgoingCall(implMethod);
            methodRepository.save(implMethod);
            result.add(nowInterfaceTypeMethod);
          }
          if (implType.getTypeKind().equals(TypeKind.INTERFACE)) {
            queue.add(implType);
          }
        }
      }
    }

    return result;
  }

  private static Method getImplMethod(
      Method nowInterfaceTypeMethod,
      Type nowInterfaceType,
      Map<MethodSignature, Method> implTypeMethods,
      Type implementationType) {
    if (nowInterfaceType.isInternal()) {
      if (implTypeMethods.containsKey(nowInterfaceTypeMethod.getMethodSignature())) {
        return implTypeMethods.get(nowInterfaceTypeMethod.getMethodSignature());
      }
      return Method.inheritedInternal(nowInterfaceTypeMethod, implementationType);
    }

    return Method.inheritedExternal(nowInterfaceTypeMethod, implementationType);
  }

  private static Map<MethodSignature, Method> getImplementationTypeDeclaredMethods(
      Type implementationType) {
    return implementationType.getMethods().stream()
        .collect(Collectors.toMap(Method::getMethodSignature, it -> it));
  }
}
