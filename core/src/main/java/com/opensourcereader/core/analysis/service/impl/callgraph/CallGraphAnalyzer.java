package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.List;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
import com.opensourcereader.core.analysis.dto.callgraph.MethodCallsOfClass;

@Component
public class CallGraphAnalyzer {

  public List<MethodCallsOfClass> createMethodCallsOfClass(List<ClassBytecode> classBytecodes) {
    return classBytecodes.stream().map(this::createMethodCallOfClass).toList();
  }

  private MethodCallsOfClass createMethodCallOfClass(ClassBytecode classBytecode) {
    ClassReader classReader = new ClassReader(classBytecode.bytes());
    CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
    classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

    return MethodCallsOfClass.of(
        classReader.getClassName(),
        classReader.getInterfaces(),
        callGraphClassVisitor.getMethodCallEdges());
  }
}
