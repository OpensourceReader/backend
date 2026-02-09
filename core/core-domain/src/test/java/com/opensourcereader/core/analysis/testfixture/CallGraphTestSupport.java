package com.opensourcereader.core.analysis.testfixture;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.domain.entity.Type;

public final class CallGraphTestSupport {

  private CallGraphTestSupport() {}

  public static List<MethodCallEdge> getOutgoingCallEdges(List<Type> types) {
    return types.stream()
        .flatMap(type -> type.getMethods().stream())
        .flatMap(method -> method.getOutgoingCalls().stream())
        .toList();
  }
}
