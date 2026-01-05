package com.opensourcereader.core.analysis.entity.method;

import static com.opensourcereader.core.analysis.entity.method.NameSeparators.PACKAGE_SEPARATOR;
import static com.opensourcereader.core.analysis.entity.method.NameSeparators.PATH_SEPARATOR;

import java.util.ArrayList;
import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import jakarta.persistence.Embeddable;

@Embeddable
public record CodeMethodSignature(String methodSignature) {

  public static CodeMethodSignature of(MethodCallInfo callee) {
    return of(
        callee.methodName(),
        callee.descriptor().argumentTypes(),
        callee.descriptor().methodReturnType());
  }

  public static CodeMethodSignature of(DeclaredMethodInfo method) {
    return of(
        method.methodName(),
        method.methodDescriptor().argumentTypes(),
        method.methodDescriptor().methodReturnType());
  }

  public static CodeMethodSignature of(CodeMethodExtractResult methodExtractResult) {
    return of(
        methodExtractResult.methodName(),
        methodExtractResult.paramTypes(),
        methodExtractResult.returnType());
  }

  public static CodeMethodSignature of(SourceCodeParseResult codeParseResult) {
    return of(
        codeParseResult.methodName(),
        codeParseResult.argumentTypes(),
        codeParseResult.returnType());
  }

  private static CodeMethodSignature of(
      String methodName, List<String> rawArgumentTypes, String returnType) {
    List<String> paramTypes = extractParamTypes(rawArgumentTypes);
    return new CodeMethodSignature(
        methodName + "(" + String.join(",", paramTypes) + ")" + returnType);
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
