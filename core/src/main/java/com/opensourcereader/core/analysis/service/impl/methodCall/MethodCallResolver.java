package com.opensourcereader.core.analysis.service.impl.methodCall;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.repository.MethodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MethodCallResolver {

  private final MethodRepository methodRepository;

  public void create(List<Type> types, List<TypeStructure> typeStructures) {
    Map<String, Map<MethodSignature, Method>> typeMethods = extractTypeMethods(types);
    Map<String, Type> typeByInternalName = extractTypes(types);
    for (MethodStructure callerMethodStructure : extractRepoMethods(typeStructures)) {
      MethodInfo callerInfo = callerMethodStructure.methodInfo();
      Method caller =
          findMethod(typeMethods, callerInfo.typeInternalName(), MethodSignature.from(callerInfo));
      if (caller == null) {
        continue;
      }
      for (MethodCallInfo calleeInfo : callerMethodStructure.calleeMethods()) {
        Method callee =
            findMethod(
                typeMethods, calleeInfo.calleeTypeInternalName(), MethodSignature.from(calleeInfo));
        if (callee == null) {
          Type type = typeByInternalName.get(calleeInfo.calleeTypeInternalName());
          if (type == null) {
            continue;
          }
          callee = methodRepository.save(Method.inheritedExternal(calleeInfo, type));

          typeMethods
              .computeIfAbsent(type.getTypeInternalName(), k -> new HashMap<>())
              .put(callee.getMethodSignature(), callee);
        }
        caller.addOutgoingCall(callee);
      }
    }
  }

  private Map<String, Type> extractTypes(List<Type> types) {
    return types.stream().collect(Collectors.toMap(Type::getTypeInternalName, type -> type));
  }

  private Method findMethod(
      Map<String, Map<MethodSignature, Method>> typeMethods,
      String typeName,
      MethodSignature signature) {
    Map<MethodSignature, Method> methods = typeMethods.get(typeName);
    if (methods == null) {
      return null;
    }
    return methods.get(signature);
  }

  private Map<String, Map<MethodSignature, Method>> extractTypeMethods(List<Type> types) {
    return types.stream()
        .collect(
            Collectors.toMap(
                Type::getTypeInternalName,
                type ->
                    type.getMethods().stream()
                        .collect(
                            Collectors.toMap(Method::getMethodSignature, Function.identity()))));
  }

  private static List<MethodStructure> extractRepoMethods(List<TypeStructure> typeStructures) {
    return typeStructures.stream()
        .flatMap(typeStructure -> typeStructure.methods().stream())
        .toList();
  }
}
