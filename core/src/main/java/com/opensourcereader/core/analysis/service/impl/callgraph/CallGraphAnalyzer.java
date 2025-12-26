package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.dto.callgraph.MethodCallsOfClass;

@Component
public class CallGraphAnalyzer {

  public List<MethodCallsOfClass> createMethodCallsOfClass(List<Path> byteCodePaths) {
    return byteCodePaths.stream().map(this::createMethodCallOfClass).toList();
  }

  private MethodCallsOfClass createMethodCallOfClass(Path byteCodeFile) {
    try (InputStream inputStream = Files.newInputStream(byteCodeFile)) {
      ClassReader classReader = new ClassReader(inputStream);
      CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
      classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

      return MethodCallsOfClass.of(
          classReader.getClassName(),
          classReader.getInterfaces(),
          callGraphClassVisitor.getMethodCallEdges());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
