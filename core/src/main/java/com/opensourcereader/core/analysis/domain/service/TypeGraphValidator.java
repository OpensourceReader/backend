package com.opensourcereader.core.analysis.domain.service;

import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.entity.TypeImplementation;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TypeGraphValidator {

  public void validateCyclic(Type startType) {
    Set<Long> visited = new HashSet<>();
    Set<Long> recursionStack = new HashSet<>();

    dfs(startType, visited, recursionStack);
  }

  private void dfs(Type current, Set<Long> visited, Set<Long> recursionStack) {

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
    for (TypeImplementation edge : current.getImplementations()) {
      Type next = edge.getImplementedType();
      dfs(next, visited, recursionStack);
    }

    recursionStack.remove(id);
  }
}
