package com.opensourcereader.core.analysis.domain.entity.method;

import static com.opensourcereader.core.analysis.domain.entity.file.FileNameSeparators.PACKAGE_SEPARATOR;
import static com.opensourcereader.core.analysis.domain.entity.file.FileNameSeparators.PATH_SEPARATOR;

import java.util.ArrayList;
import java.util.List;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.external.ExternalMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeDeclaredMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.SourceCodeParseResult;
import jakarta.persistence.Embeddable;

@Embeddable
public record MethodSignature(String methodSignature) {

  public static MethodSignature from(ExternalMethodInfo externalMethodInfo) {
    return create(
        externalMethodInfo.methodName(),
        externalMethodInfo.descriptor().argumentTypes(),
        externalMethodInfo.descriptor().methodReturnType());
  }

  public static MethodSignature from(MethodCallInfo callee) {
    return create(
        callee.methodName(),
        callee.descriptor().argumentTypes(),
        callee.descriptor().methodReturnType());
  }

  public static MethodSignature from(ByteCodeDeclaredMethodInfo method) {
    return create(
        method.methodName(),
        method.methodDescriptor().argumentTypes(),
        method.methodDescriptor().methodReturnType());
  }

  public static MethodSignature from(MethodInfo methodInfo) {
    return create(
        methodInfo.methodName(),
        methodInfo.methodDescriptor().argumentTypes(),
        methodInfo.methodDescriptor().methodReturnType());
  }

  public static MethodSignature from(SourceCodeParseResult codeParseResult) {
    return create(
        codeParseResult.methodName(),
        codeParseResult.argumentTypes(),
        codeParseResult.returnType());
  }

  private static MethodSignature create(
      String methodName, List<String> rawArgumentTypes, String returnType) {
    List<String> paramTypes = extractArgumentTypeLastNames(rawArgumentTypes);
    return new MethodSignature(methodName + "(" + String.join(",", paramTypes) + ")" + returnType);
  }

  private static List<String> extractArgumentTypeLastNames(List<String> rawArgumentTypes) {
    List<String> argumentTypes = new ArrayList<>();
    for (String rawArgumentType : rawArgumentTypes) {
      String paramType = getArgumentTypeLastName(rawArgumentType);
      argumentTypes.add(paramType);
    }
    return argumentTypes;
  }

  private static String getArgumentTypeLastName(String argumentType) {
    if (argumentType.contains(PATH_SEPARATOR)) {
      return argumentType.substring(argumentType.lastIndexOf(PATH_SEPARATOR) + 1);
    }
    if (argumentType.contains(PACKAGE_SEPARATOR)) {
      return argumentType.substring(argumentType.lastIndexOf(PACKAGE_SEPARATOR) + 1);
    }
    return argumentType;
  }
}
