package com.opensourcereader.core.analysis.dto;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeMethodStructure;
import com.opensourcereader.core.analysis.infra.dto.ParsedSourceFile;

public record MethodStructure(MethodInfo methodInfo, List<MethodCallInfo> calleeMethods) {

  public static MethodStructure of(
      ByteCodeMethodStructure byteCodeMethodStructure, ParsedSourceFile parsedSourceFile) {
    MethodSignature signature =
        MethodSignature.from(byteCodeMethodStructure.byteCodeDeclaredMethodInfo());
    MethodInfo methodInfo =
        MethodInfo.of(
            byteCodeMethodStructure.byteCodeDeclaredMethodInfo(),
            parsedSourceFile.methods().get(signature));
    return new MethodStructure(methodInfo, byteCodeMethodStructure.calleeMethods());
  }
}
