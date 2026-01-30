package com.opensourcereader.core.analysis.service.impl.methodcall;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.DeclaredTypeImplementEdge;

@Component
public class DeclaredTypeGraphValidator {

  public void validateAcyclic(DeclaredType startType) {
    Set<Long> visited = new HashSet<>();
    Set<Long> recursionStack = new HashSet<>();

    dfs(startType, visited, recursionStack);
  }

  private void dfs(DeclaredType current, Set<Long> visited, Set<Long> recursionStack) {

    Long id = current.getId();

    // 현재 경로에 이미 존재 → 사이클
    if (recursionStack.contains(id)) {
      throw new IllegalStateException(
          "Cyclic implementation detected at type: " + current.getTypeInternalName());
    }

    // 이미 완전히 검사 끝난 노드면 스킵
    if (!visited.add(id)) {
      return;
    }

    recursionStack.add(id);

    // 구현 관계 탐색
    for (DeclaredTypeImplementEdge edge : current.getImplementations()) {
      DeclaredType next = edge.getType();
      dfs(next, visited, recursionStack);
    }

    recursionStack.remove(id);
  }
}
