package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.List;

import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;

public record DeclaredMethodEdges(
    DeclaredMethod caller,
    List<DeclaredMethod> calleeMethods,
    DeclaredMethod superMethod,
    List<DeclaredMethod> interfaces) {}
