package com.opensourcereader.core.analysis.testfixture;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.entity.method.Method;
import com.opensourcereader.core.analysis.entity.method.MethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;

public final class CallGraphTestSupport {

  private CallGraphTestSupport() {}

  public static List<MethodCallEdge> getOutgoingCallEdges(List<Method> methods) {
    return methods.stream().flatMap(method -> method.getOutgoingCalls().stream()).toList();
  }

  public static DeclaredMethodInfo getDeclaredMethodInfo(
      String typeName, String methodName, MethodDescriptor methodDescriptor) {
    return new DeclaredMethodInfo(
        typeName,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        methodDescriptor,
        null,
        null,
        1,
        1);
  }
}
