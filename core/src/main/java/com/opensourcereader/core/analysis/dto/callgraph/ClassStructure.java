package com.opensourcereader.core.analysis.dto.callgraph;

import java.util.List;

import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;

public record ClassStructure(ClassInfo classInfo, List<MethodStructure> methods) {}
