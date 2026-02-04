package com.opensourcereader.core.analysis.dto.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;

public record ExternalTypeStructure(
    ExternalTypeInfo externalTypeInfo, List<ExternalMethodInfo> externalMethodInfos) {

  public static List<ExternalTypeStructure> fromMethodCalls(
      Set<MethodCallInfo> externalMethodCalls) {
    Map<ExternalTypeInfo, Set<ExternalMethodInfo>> externalTypeInfoSetMap =
        externalMethodCalls.stream()
            .map(ExternalTypeStructure::fromMethodCall)
            .collect(
                Collectors.groupingBy(
                    ExternalMethoCallStructure::externalTypeInfo,
                    Collectors.mapping(
                        ExternalMethoCallStructure::externalMethodInfo, Collectors.toSet())));

    return externalTypeInfoSetMap.entrySet().stream()
        .map(
            externalMethodCall ->
                new ExternalTypeStructure(
                    externalMethodCall.getKey(), new ArrayList<>(externalMethodCall.getValue())))
        .collect(Collectors.toList());
  }

  public static List<ExternalTypeStructure> fromParentTypes(Set<String> parentTypeNames) {
    return parentTypeNames.stream()
        .map(
            parentTypeName ->
                new ExternalTypeStructure(new ExternalTypeInfo(parentTypeName), List.of()))
        .toList();
  }

  private static ExternalMethoCallStructure fromMethodCall(MethodCallInfo externalMethodCall) {
    ExternalTypeInfo externalTypeInfo =
        new ExternalTypeInfo(externalMethodCall.calleeTypeInternalName());
    ExternalMethodInfo externalMethodInfo =
        new ExternalMethodInfo(
            externalMethodCall.calleeTypeInternalName(),
            externalMethodCall.methodName(),
            externalMethodCall.descriptor());
    return new ExternalMethoCallStructure(externalTypeInfo, externalMethodInfo);
  }

  private record ExternalMethoCallStructure(
      ExternalTypeInfo externalTypeInfo, ExternalMethodInfo externalMethodInfo) {}
}
