package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.entity.Extension.appendExtension;

import java.util.Arrays;
import java.util.List;

import com.opensourcereader.core.analysis.entity.Extension;

public record ClassMethodCallResult(
    String classPath, List<String> linkedInterfacePaths, List<MethodCall> methodCalls) {

  public static ClassMethodCallResult of(
      String classInternalName, String[] interfaces, List<MethodCall> methodCalls) {
    List<String> interfacePaths =
        Arrays.stream(interfaces).map(inter -> appendExtension(inter, Extension.JAVA)).toList();
    return new ClassMethodCallResult(
        appendExtension(classInternalName, Extension.JAVA), interfacePaths, methodCalls);
  }
}
