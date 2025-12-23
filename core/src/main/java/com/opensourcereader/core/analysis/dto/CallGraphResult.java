package com.opensourcereader.core.analysis.dto;

import static com.opensourcereader.core.analysis.dto.ExtensionConstant.JAVA_EXTENSION;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record CallGraphResult(
    String classPath,
    List<String> interfacePaths,
    Map<String, Set<CalleePathAndMethodDescriptor>> edges) {

  public static CallGraphResult of(
      String classInternalName,
      String[] interfaces,
      Map<String, Set<CalleePathAndMethodDescriptor>> edges) {
    List<String> interfacePaths =
        Arrays.stream(interfaces).map(inter -> inter + JAVA_EXTENSION).toList();
    return new CallGraphResult(classInternalName + JAVA_EXTENSION, interfacePaths, edges);
  }
}
