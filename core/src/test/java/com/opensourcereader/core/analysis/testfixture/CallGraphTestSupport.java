package com.opensourcereader.core.analysis.testfixture;

import java.util.List;

import com.opensourcereader.core.analysis.entity.Method;
import com.opensourcereader.core.analysis.entity.MethodCallEdge;

public final class CallGraphTestSupport {

  private CallGraphTestSupport() {}

  public static List<MethodCallEdge> getOutgoingCallEdges(List<Method> methods) {
    return methods.stream().flatMap(method -> method.getOutgoingCalls().stream()).toList();
  }
}
