package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodCallGraphServiceImpl implements CodeMethodCallGraphService {

  private final MethodPolymorphicDispatchService methodPolymorphicDispatchService;
  private final MethodCallEdgeService methodCallEdgeService;
  private final CodeMethodRepository codeMethodRepository;

  @Transactional
  @Override
  public List<CodeMethodCallEdge> createMethodCallGraph(
      Long repoId, List<ClassStructure> classStructures) {
    List<CodeMethodCallEdge> dispatch = methodPolymorphicDispatchService.dispatch(repoId);
    List<CodeMethodCallEdge> codeMethodCallEdges =
        methodCallEdgeService.create(repoId, classStructures);

    List<CodeMethodCallEdge> result = new ArrayList<>(dispatch);
    result.addAll(codeMethodCallEdges);
    return result;
  }

  @Transactional
  @Override
  public DeclaredMethod getCodeMethodById(Long codeMethodId) {
    DeclaredMethod declaredMethod =
        codeMethodRepository
            .findWithOutgoingGraphById(codeMethodId)
            .orElseThrow(IllegalArgumentException::new);
    codeMethodRepository
        .findWithIngoingGraphById(declaredMethod.getId())
        .orElseThrow(IllegalArgumentException::new);

    return declaredMethod;
  }
}
