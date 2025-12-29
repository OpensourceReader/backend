package com.opensourcereader.core.analysis.entity.codedetail;

import java.util.ArrayList;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import jakarta.persistence.Embeddable;

@Embeddable
public record CodeMethodSignature(String methodSignature) {

  private static final String PATH_SEPARATOR = "/";
  private static final String PACKAGE_SEPARATOR = ".";

  public static CodeMethodSignature of(DeclaredMethodInfo method) {
    return of(method.methodName(), method.methodDescriptor().argumentTypes());
  }

  public static CodeMethodSignature of(String methodName, List<String> rawArgumentTypes) {
    List<String> paramTypes = extractParamTypes(rawArgumentTypes);
    return new CodeMethodSignature(methodName + "(" + String.join(",", paramTypes) + ")");
  }

  private static List<String> extractParamTypes(List<String> rawArgumentTypes) {
    List<String> argumentTypes = new ArrayList<>();
    for (String rawArgumentType : rawArgumentTypes) {
      String paramType = getArgumentClassName(rawArgumentType);
      argumentTypes.add(paramType);
    }
    return argumentTypes;
  }

  private static String getArgumentClassName(String argumentType) {
    if (argumentType.contains(PATH_SEPARATOR)) {
      return argumentType.substring(argumentType.lastIndexOf(PATH_SEPARATOR) + 1);
    }
    if (argumentType.contains(PACKAGE_SEPARATOR)) {
      return argumentType.substring(argumentType.lastIndexOf(PACKAGE_SEPARATOR) + 1);
    }
    return argumentType;
  }
}
