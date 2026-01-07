package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.repository.CodeMethodCallEdgeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodCallEdgeService {

  private final CodeMethodCallEdgeRepository codeMethodCallEdgeRepository;

  public List<CodeMethodCallEdge> create(Long repoId, List<ClassStructure> classStructures) {
    return List.of();
  }
}
