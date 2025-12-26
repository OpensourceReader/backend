package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.entity.Extension.appendExtension;

import java.util.Arrays;
import java.util.List;

import com.opensourcereader.core.analysis.entity.Extension;

public record MethodCallsOfClass(
    String classPath, List<String> linkedInterfacePaths, List<MethodCallEdge> methodCallEdges) {

  public static MethodCallsOfClass of(
      String classInternalName, String[] interfaces, List<MethodCallEdge> methodCallEdges) {
    List<String> interfacePaths =
        Arrays.stream(interfaces).map(inter -> appendExtension(inter, Extension.JAVA)).toList();
    return new MethodCallsOfClass(
        appendExtension(classInternalName, Extension.JAVA), interfacePaths, methodCallEdges);
  }
}
