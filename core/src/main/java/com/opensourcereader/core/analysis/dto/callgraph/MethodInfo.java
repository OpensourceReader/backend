package com.opensourcereader.core.analysis.dto.callgraph;

import static com.opensourcereader.core.analysis.entity.Extension.appendExtension;

import java.util.Arrays;
import java.util.List;

import aj.org.objectweb.asm.Type;
import com.opensourcereader.core.analysis.entity.Extension;

public record MethodInfo(
    String classPath, String methodName, String methodReturnType, List<String> argumentTypes) {
  public static MethodInfo of(String className, String methodName, String descriptor) {
    return new MethodInfo(
        appendExtension(className, Extension.JAVA),
        methodName,
        getReturnType(descriptor),
        getArgumentTypes(descriptor));
  }

  private static String getReturnType(String descriptor) {
    return Type.getReturnType(descriptor).getClassName();
  }

  private static List<String> getArgumentTypes(String descriptor) {
    return Arrays.stream(Type.getArgumentTypes(descriptor)).map(Type::getClassName).toList();
  }
}
