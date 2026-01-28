package com.opensourcereader.core.analysis.infra.dto;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;

public record ByteCodeMethodStructure(
    ByteCodeDeclaredMethodInfo byteCodeDeclaredMethodInfo,
    List<TypeStructureMeta.MethodCallInfo> calleeMethods) {}
