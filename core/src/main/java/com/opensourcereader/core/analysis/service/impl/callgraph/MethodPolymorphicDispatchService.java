package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.repository.CodeMethodCallEdgeRepository;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MethodPolymorphicDispatchService {

  private final CodeMethodCallEdgeRepository codeMethodCallEdgeRepository;
  private final DeclaredTypeRepository declaredTypeRepository;

  public List<CodeMethodCallEdge> dispatch(Long repoId) {
    List<CodeMethodCallEdge> codeMethodCallEdges = new ArrayList<>();
    List<DeclaredType> interfaceTypes =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.INTERFACE);
    for (DeclaredType interfaceType : interfaceTypes) {
      codeMethodCallEdges.addAll(dispatchByBfs(interfaceType, true));
    }
    List<DeclaredType> classTypes =
        declaredTypeRepository.findByRepoAndTypesByKind(repoId, TypeKind.CLASS);
    for (DeclaredType classType : classTypes) {
      codeMethodCallEdges.addAll(dispatchByBfs(classType, false));
    }

    return codeMethodCallEdgeRepository.saveAll(codeMethodCallEdges);
  }

  private List<CodeMethodCallEdge> dispatchByBfs(DeclaredType type, boolean isInterface) {
    List<CodeMethodCallEdge> result = new ArrayList<>();
    Queue<DeclaredType> queue = new LinkedList<>();
    queue.add(type);

    while (!queue.isEmpty()) {
      int n = queue.size();
      for (int i = 0; i < n; i++) {
        DeclaredType polledType = queue.poll();
        for (DeclaredTypeImplementEdge implementEdge : polledType.getImplementations()) {
          DeclaredType declaredType = implementEdge.getInterfaceType();
          Map<DeclaredMethod, DeclaredMethod> collect =
              declaredType.getDeclaredMethods().stream()
                  .collect(Collectors.toMap(it -> it, it -> it));
          for (DeclaredMethod declaredMethod : polledType.getDeclaredMethods()) {
            DeclaredMethod internalInheritance =
                DeclaredMethod.internalInheritance(declaredMethod, declaredType);
            DeclaredMethod nextDeclaredMethod =
                collect.getOrDefault(declaredMethod, internalInheritance);
            result.add(CodeMethodCallEdge.of(declaredMethod, nextDeclaredMethod));
          }

          //          visited 없어서 중복 방문/무한 루프 위험, 인터페이스는 보통은 dag
          if (isInterface && declaredType.getTypeKind().equals(TypeKind.INTERFACE)) {
            queue.add(declaredType);
          }
          if (!isInterface && declaredType.getTypeKind().equals(TypeKind.CLASS)) {
            queue.add(declaredType);
          }
        }
      }
    }

    return result;
  }
}
