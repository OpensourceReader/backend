package com.opensourcereader.core.analysis.infra.bytecode;

import java.util.List;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.infra.dto.ClassBytecode;

@Component
public class ClassStructureExtractor {

  public List<ByteCodeClassStructure> extract(List<ClassBytecode> classBytecodes) {
    return classBytecodes.stream().map(this::extractFrom).toList();
  }

  private ByteCodeClassStructure extractFrom(ClassBytecode classBytecode) {
    ClassReader classReader = new ClassReader(classBytecode.bytes());
    ClassStructureVisitor classStructureVisitor = new ClassStructureVisitor();
    classReader.accept(classStructureVisitor, ClassReader.SKIP_DEBUG);

    return new ByteCodeClassStructure(
        classStructureVisitor.getTypeInfo(), classStructureVisitor.getByteCodeMethodStructures());
  }
}
