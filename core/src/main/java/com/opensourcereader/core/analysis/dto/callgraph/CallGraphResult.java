package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.dto.callgraph.ExtensionConstant.JAVA_EXTENSION;

import java.util.Arrays;
import java.util.List;

public record CallGraphResult(
    String classPath, List<String> linkedInterfacePaths, List<RawMethodCall> rawMethodCalls) {

  public static CallGraphResult of(
      String classInternalName, String[] interfaces, List<RawMethodCall> rawMethodCalls) {
    List<String> interfacePaths =
        Arrays.stream(interfaces).map(inter -> inter + JAVA_EXTENSION).toList();
    return new CallGraphResult(classInternalName + JAVA_EXTENSION, interfacePaths, rawMethodCalls);
  }
}
