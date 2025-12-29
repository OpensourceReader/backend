package com.opensourcereader.core.analysis.infra.bytecode;

import java.util.List;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.dto.callgraph.ClassBytecode;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;

@Component
public class ClassStructureExtractor {

  public List<ClassStructure> extract(List<ClassBytecode> classBytecodes) {
    return classBytecodes.stream().map(this::extractFrom).toList();
  }

  private ClassStructure extractFrom(ClassBytecode classBytecode) {
    ClassReader classReader = new ClassReader(classBytecode.bytes());
    ClassStructureVisitor classStructureVisitor = new ClassStructureVisitor();
    classReader.accept(classStructureVisitor, ClassReader.SKIP_DEBUG);

    return new ClassStructure(
        classStructureVisitor.getClassInfo(), classStructureVisitor.getMethodStructures());
  }
}
