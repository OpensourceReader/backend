package com.opensourcereader.core.analysis.infra.bytecode;

import java.util.List;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;

@Component
public class CallGraphAnalyzer {

  public List<ClassStructure> createMethodCallsOfClass(List<ClassBytecode> classBytecodes) {
    return classBytecodes.stream().map(this::createMethodCallOfClass).toList();
  }

  private ClassStructure createMethodCallOfClass(ClassBytecode classBytecode) {
    ClassReader classReader = new ClassReader(classBytecode.bytes());
    CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
    classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

    return new ClassStructure(
        callGraphClassVisitor.getClassInfo(), callGraphClassVisitor.getMethodStructures());
  }
}
