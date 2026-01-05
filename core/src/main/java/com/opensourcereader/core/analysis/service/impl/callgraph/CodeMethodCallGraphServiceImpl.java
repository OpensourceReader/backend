package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;
import com.opensourcereader.core.analysis.entity.method.CodeMethod;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.repository.CodeMethodCallEdgeRepository;
import com.opensourcereader.core.analysis.repository.CodeMethodRepository;
import com.opensourcereader.core.analysis.service.CodeMethodCallGraphService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeMethodCallGraphServiceImpl implements CodeMethodCallGraphService {

  private final CodeMethodCallEdgeRepository codeMethodCallEdgeRepository;
  private final CodeMethodRepository codeMethodRepository;
  private final DeclaredMethodEdgeResolver declaredMethodEdgeResolver;

  @Transactional
  @Override
  public List<CodeMethodCallEdge> createMethodCallGraph(
      Long repoId, List<ClassStructure> classStructures) {
    List<CodeMethodCallEdge> result = new ArrayList<>();
    for (ClassStructure classStructure : classStructures) {
      //      declaredMethodEdgeResolver.resolve();

    }
    return codeMethodCallEdgeRepository.saveAll(result);
  }

  private Map<CodeMethodSignature, MethodStructure> getMethodStructures(
      ClassStructure classStructure) {
    return classStructure.methods().stream()
        .collect(
            Collectors.toMap(
                methodStructure -> CodeMethodSignature.of(methodStructure.declaredMethodInfo()),
                methodStructure -> methodStructure));
  }

  @Transactional
  @Override
  public CodeMethod getCodeMethodById(Long codeMethodId) {
    CodeMethod codeMethod =
        codeMethodRepository
            .findWithOutgoingGraphById(codeMethodId)
            .orElseThrow(IllegalArgumentException::new);
    codeMethodRepository
        .findWithIngoingGraphById(codeMethod.getId())
        .orElseThrow(IllegalArgumentException::new);

    return codeMethod;
  }

  private List<CodeMethod> getDeclaredMethods(Long repoId, ClassStructure classStructure) {
    return classStructure.methods().stream()
        .map(
            methodStructure -> {
              CodeMethodSignature codeMethodSignature =
                  CodeMethodSignature.of(methodStructure.declaredMethodInfo());
              return codeMethodRepository.findByRepoIdAndClassInternalNameAndMethodSignature(
                  repoId,
                  methodStructure.declaredMethodInfo().className(),
                  codeMethodSignature.methodSignature());
            })
        .flatMap(Optional::stream)
        .toList();
  }
}
