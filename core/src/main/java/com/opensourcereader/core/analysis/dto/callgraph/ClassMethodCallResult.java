package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.dto.callgraph.ExtensionConstant.JAVA_EXTENSION;

import java.util.Arrays;
import java.util.List;

public record ClassMethodCallResult(
    String classPath, List<String> linkedInterfacePaths, List<MethodCall> methodCalls) {

  public static ClassMethodCallResult of(
      String classInternalName, String[] interfaces, List<MethodCall> methodCalls) {
    List<String> interfacePaths =
        Arrays.stream(interfaces).map(inter -> inter + JAVA_EXTENSION).toList();
    return new ClassMethodCallResult(
        classInternalName + JAVA_EXTENSION, interfacePaths, methodCalls);
  }
}
