package com.opensourcereader.core.analysis.dto.callgraph.method;

import java.util.List;

public record MethodStructure(
    DeclaredMethodInfo declaredMethodInfo, List<MethodCallInfo> calleeMethods) {}
