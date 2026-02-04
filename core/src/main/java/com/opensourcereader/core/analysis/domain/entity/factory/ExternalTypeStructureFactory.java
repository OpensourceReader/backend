package com.opensourcereader.core.analysis.domain.entity.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.dto.external.ExternalTypeInfo;
import com.opensourcereader.core.analysis.dto.external.ExternalTypeStructure;

@Component
public class ExternalTypeStructureFactory {

  public List<ExternalTypeStructure> create(List<Type> types, List<TypeStructure> typeStructures) {
    Set<String> internalDeclaredTypeNames =
        types.stream().map(Type::getTypeInternalName).collect(Collectors.toUnmodifiableSet());
    Set<MethodCallInfo> calleeMethodCalls =
        typeStructures.stream()
            .flatMap(typeStructure -> typeStructure.methods().stream())
            .flatMap(methodStructure -> methodStructure.calleeMethods().stream())
            .filter(
                methodInfo ->
                    !internalDeclaredTypeNames.contains(methodInfo.calleeTypeInternalName()))
            .collect(Collectors.toSet());
    Set<String> parentTypeNames =
        collectParentTypeNames(typeStructures).stream()
            .filter(parentTypeName -> !internalDeclaredTypeNames.contains(parentTypeName))
            .collect(Collectors.toSet());
    return resolveExternalTypeStructures(parentTypeNames, calleeMethodCalls);
  }

  private Set<String> collectParentTypeNames(List<TypeStructure> typeStructures) {
    Set<String> result = new HashSet<>();
    for (TypeStructure ts : typeStructures) {
      TypeInfo info = ts.typeInfo();

      if (info.superName() != null) {
        result.add(info.superName());
      }
      if (info.interfaceNames() != null) {
        result.addAll(info.interfaceNames());
      }
    }

    return result;
  }

  private List<ExternalTypeStructure> resolveExternalTypeStructures(
      Set<String> parentTypeNames, Set<MethodCallInfo> calleeMethodCalls) {
    List<ExternalTypeStructure> parentTypes =
        ExternalTypeStructure.fromParentTypes(parentTypeNames);
    List<ExternalTypeStructure> methodCallTypes =
        ExternalTypeStructure.fromMethodCalls(calleeMethodCalls);
    Set<ExternalTypeInfo> existingTypeInfos =
        methodCallTypes.stream()
            .map(ExternalTypeStructure::externalTypeInfo)
            .collect(Collectors.toSet());
    List<ExternalTypeStructure> exceptTypes =
        parentTypes.stream()
            .filter(parent -> !existingTypeInfos.contains(parent.externalTypeInfo()))
            .toList();

    List<ExternalTypeStructure> merged = new ArrayList<>(methodCallTypes);
    merged.addAll(exceptTypes);
    return merged;
  }
}
