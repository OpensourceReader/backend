package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.entity.Extension.appendExtension;

import java.util.Arrays;
import java.util.List;

import aj.org.objectweb.asm.Type;
import com.opensourcereader.core.analysis.entity.Extension;

public record MethodCallEdge(
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

  public static MethodCallEdge of(
      String callerClassName,
      String callerMethodName,
      String callerDescriptor,
      String calleeClassName,
      String calleeMethodName,
      String calleeDescription,
      int operationCode,
      boolean isCalleeMethodInterface) {
    return new MethodCallEdge(
        appendExtension(callerClassName, Extension.JAVA),
        callerMethodName,
        getReturnType(callerDescriptor),
        getArgumentTypes(callerDescriptor),
        appendExtension(calleeClassName, Extension.JAVA),
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
