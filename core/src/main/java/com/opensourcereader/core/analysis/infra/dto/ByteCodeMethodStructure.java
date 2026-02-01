package com.opensourcereader.core.analysis.infra.dto;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeStructureMeta;

public record ByteCodeMethodStructure(
    ByteCodeDeclaredMethodInfo byteCodeDeclaredMethodInfo,
    List<MethodCallInfo> calleeMethods) {}
