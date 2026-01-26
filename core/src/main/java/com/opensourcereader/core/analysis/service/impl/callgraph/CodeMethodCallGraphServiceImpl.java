package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodCallGraphServiceImpl implements CodeMethodCallGraphService {

  private final MethodPolymorphicDispatcher methodPolymorphicDispatcher;
  private final MethodCallResolver methodCallResolver;
  private final CodeMethodRepository codeMethodRepository;

  @Transactional
  @Override
  public void createMethodCallGraph(Long repoId, List<ClassStructure> classStructures) {
    methodPolymorphicDispatcher.dispatch(repoId);
    methodCallResolver.create(repoId, classStructures);
  }

  @Transactional
  @Override
  public DeclaredMethod getCodeMethodById(Long codeMethodId) {
    codeMethodRepository
        .findWithOutgoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);

    return codeMethodRepository
        .findWithIngoingGraphById(codeMethodId)
        .orElseThrow(IllegalArgumentException::new);
  }
}
