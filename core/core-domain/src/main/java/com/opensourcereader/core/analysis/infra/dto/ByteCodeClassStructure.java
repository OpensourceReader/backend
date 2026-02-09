package com.opensourcereader.core.analysis.infra.dto;

import java.util.List;

import com.opensourcereader.core.analysis.dto.TypeInfo;

public record ByteCodeClassStructure(TypeInfo typeInfo, List<ByteCodeMethodStructure> methods) {}
