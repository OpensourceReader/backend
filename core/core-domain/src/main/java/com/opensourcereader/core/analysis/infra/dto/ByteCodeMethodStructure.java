package com.opensourcereader.core.analysis.infra.dto;

import java.util.List;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;

public record ByteCodeMethodStructure(
    ByteCodeDeclaredMethodInfo byteCodeDeclaredMethodInfo, List<MethodCallInfo> calleeMethods) {}
