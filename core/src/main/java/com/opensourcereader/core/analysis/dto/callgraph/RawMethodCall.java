package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.dto.callgraph.ExtensionConstant.JAVA_EXTENSION;

import java.util.Arrays;
import java.util.List;

import aj.org.objectweb.asm.Type;

public record RawMethodCall(
    String callerClassPath,
    String callerMethodName,
    String callerMethodReturnType,
    List<String> callerRawArgumentTypes,
    String calleeClassPath,
    String calleeMethodName,
    String calleeMethodReturnType,
    List<String> calleeRawArgumentTypes,
    int operationCode,
    boolean isCalleeMethodInterface) {

  public static RawMethodCall of(
      String callerClassName,
      String callerMethodName,
      String callerDescriptor,
      String calleeClassName,
      String calleeMethodName,
      String calleeDescription,
      int operationCode,
      boolean isCalleeMethodInterface) {
    return new RawMethodCall(
        callerClassName + JAVA_EXTENSION,
        callerMethodName,
        getReturnType(callerDescriptor),
        getArgumentTypes(callerDescriptor),
        calleeClassName + JAVA_EXTENSION,
        calleeMethodName,
        getReturnType(calleeDescription),
        getArgumentTypes(calleeDescription),
        operationCode,
        isCalleeMethodInterface);
  }

  private static String getReturnType(String descriptor) {
    return Type.getReturnType(descriptor).getClassName();
  }

  private static List<String> getArgumentTypes(String descriptor) {
    return Arrays.stream(Type.getArgumentTypes(descriptor)).map(Type::getClassName).toList();
  }
}
