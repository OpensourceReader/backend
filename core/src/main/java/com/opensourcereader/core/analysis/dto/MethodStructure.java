package com.opensourcereader.core.analysis.dto;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeMethodStructure;
import com.opensourcereader.core.analysis.infra.dto.ParsedSourceFile;

public record MethodStructure(DeclaredMethodInfo methodInfo, List<MethodCallInfo> calleeMethods) {

  public static MethodStructure of(
      ByteCodeMethodStructure byteCodeMethodStructure, ParsedSourceFile parsedSourceFile) {
    CodeMethodSignature signature =
        CodeMethodSignature.of(byteCodeMethodStructure.byteCodeDeclaredMethodInfo());
    DeclaredMethodInfo declaredMethodInfo =
        DeclaredMethodInfo.of(
            byteCodeMethodStructure.byteCodeDeclaredMethodInfo(),
            parsedSourceFile.methods().get(signature));
    return new MethodStructure(declaredMethodInfo, byteCodeMethodStructure.calleeMethods());
  }
}
