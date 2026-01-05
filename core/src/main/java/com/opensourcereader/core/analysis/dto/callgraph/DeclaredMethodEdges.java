package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.List;

import com.opensourcereader.core.analysis.entity.method.CodeMethod;

public record DeclaredMethodEdges(
    CodeMethod caller,
    List<CodeMethod> calleeMethods,
    CodeMethod superMethod,
    List<CodeMethod> interfaces) {}
