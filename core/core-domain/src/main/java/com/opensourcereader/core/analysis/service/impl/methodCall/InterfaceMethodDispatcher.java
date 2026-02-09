package com.opensourcereader.core.analysis.service.impl.methodCall;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.entity.TypeImplementation;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.MethodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterfaceMethodDispatcher {

  private final TypeGraphValidator typeGraphValidator;
  private final MethodRepository methodRepository;

  public void connectInterfaceImplementations(List<Type> types) {
    List<Type> interfaces =
        types.stream().filter(type -> type.isSameTypeKind(TypeKind.INTERFACE)).toList();

    for (Type interfaceType : interfaces) {
      connectMethodsToImplementation(interfaceType);
    }
  }

  private void connectMethodsToImplementation(Type interfaceType) {
    typeGraphValidator.validateCyclic(interfaceType);
    Queue<Type> queue = new LinkedList<>();
    queue.add(interfaceType);

    while (!queue.isEmpty()) {
      int n = queue.size();
      for (int i = 0; i < n; i++) {
        Type polledInterfaceType = queue.poll();
        for (TypeImplementation implEdge : polledInterfaceType.getImplementations()) {
          Type implType = implEdge.getImplementedType();
          Map<MethodSignature, Method> implTypeMethods = indexMethodsBySignature(implType);
          for (Method interfaceTypeMethod : polledInterfaceType.getMethods()) {
            MethodSignature signature = interfaceTypeMethod.getMethodSignature();
            Method implMethod =
                implTypeMethods.computeIfAbsent(
                    signature,
                    sig ->
                        methodRepository.save(
                            Method.createVirtual(
                                interfaceTypeMethod.getType(), interfaceTypeMethod, implType)));
            interfaceTypeMethod.addOutgoingCall(implMethod);
          }
          if (implType.getTypeKind().equals(TypeKind.INTERFACE)) {
            queue.add(implType);
          }
        }
      }
    }
  }

  private Map<MethodSignature, Method> indexMethodsBySignature(Type implementationType) {
    return implementationType.getMethods().stream()
        .collect(Collectors.toMap(Method::getMethodSignature, it -> it));
  }
}
