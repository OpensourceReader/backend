package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.List;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodCallEdgeRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodPolymorphicDispatcher {

  private final CodeMethodCallEdgeRepository codeMethodCallEdgeRepository;
  private final DeclaredTypeRepository declaredTypeRepository;
  private final PolymorphicEdgeComputer edgeComputer;

  public List<CodeMethodCallEdge> dispatch(Long repoId) {
    List<DeclaredType> interfaces =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.INTERFACE);
    List<DeclaredType> classes =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);

    List<CodeMethodCallEdge> edges = edgeComputer.compute(interfaces, classes);
    return codeMethodCallEdgeRepository.saveAll(edges);
  }
}
