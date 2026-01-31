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
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InterfaceImplementationLinker {

  private final TypeGraphValidator typeGraphValidator;
  private final MethodRepository methodRepository;
  private final TypeRepository typeRepository;

  public List<Method> linkAllInterfaceImplementations(Long repoId) {
    List<Type> interfaces = typeRepository.findByRepoAndTypesByKind(repoId, TypeKind.INTERFACE);

    Set<Method> result = new HashSet<>();
    for (Type interfaceType : interfaces) {
      typeGraphValidator.validateAcyclic(interfaceType);
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
        Type polledInterfaceType = queue.poll();
        for (TypeImplementation implEdge : polledInterfaceType.getImplementations()) {
          Type implType = implEdge.getImplementedType();
          Map<MethodSignature, Method> implTypeMethods = getImplementationTypeMethods(implType);
          for (Method interfaceTypeMethod : polledInterfaceType.getMethods()) {
            Method implMethod = implTypeMethods.get(interfaceTypeMethod.getMethodSignature());
            if (implMethod == null) {
              implType.addImplementationVirtualMethod(interfaceTypeMethod);
              typeRepository.save(implType);
            }
            interfaceTypeMethod.addOutgoingCall(implMethod);
            result.add(interfaceTypeMethod);
          }
          if (implType.getTypeKind().equals(TypeKind.INTERFACE)) {
            queue.add(implType);
          }
        }
      }
    }

    return result;
  }

  private Map<MethodSignature, Method> getImplementationTypeMethods(Type implementationType) {
    return implementationType.getMethods().stream()
        .collect(Collectors.toMap(Method::getMethodSignature, it -> it));
  }
}
