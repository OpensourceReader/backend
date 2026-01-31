package com.opensourcereader.core.analysis.entity.method;

import static com.opensourcereader.core.analysis.entity.content.ContentNameSeparators.PACKAGE_SEPARATOR;
import static com.opensourcereader.core.analysis.entity.content.ContentNameSeparators.PATH_SEPARATOR;

import java.util.ArrayList;
import java.util.List;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta.MethodCallInfo;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeDeclaredMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.SourceCodeParseResult;
import jakarta.persistence.Embeddable;

@Embeddable
public record MethodSignature(String methodSignature) {

  public static MethodSignature of(MethodCallInfo callee) {
    return of(
        callee.methodName(),
        callee.descriptor().argumentTypes(),
        callee.descriptor().methodReturnType());
  }

  public static MethodSignature of(ByteCodeDeclaredMethodInfo method) {
    return of(
        method.methodName(),
        method.methodDescriptor().argumentTypes(),
        method.methodDescriptor().methodReturnType());
  }

  public static MethodSignature of(DeclaredMethodInfo declaredMethodInfo) {
    return of(
        declaredMethodInfo.methodName(),
        declaredMethodInfo.methodDescriptor().argumentTypes(),
        declaredMethodInfo.methodDescriptor().methodReturnType());
  }

  public static MethodSignature of(SourceCodeParseResult codeParseResult) {
    return of(
        codeParseResult.methodName(),
        codeParseResult.argumentTypes(),
        codeParseResult.returnType());
  }

  private static MethodSignature of(
      String methodName, List<String> rawArgumentTypes, String returnType) {
    List<String> paramTypes = extractParamTypes(rawArgumentTypes);
    return new MethodSignature(methodName + "(" + String.join(",", paramTypes) + ")" + returnType);
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
