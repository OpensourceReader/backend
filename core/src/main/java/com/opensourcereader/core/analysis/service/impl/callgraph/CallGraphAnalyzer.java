package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.dto.callgraph.ClassMethodCallResult;

@Component
public class CallGraphAnalyzer {

  public List<ClassMethodCallResult> analyzeByteCodeFiles(List<Path> byteCodePaths) {
    return byteCodePaths.stream().map(this::analyzeByteCodeFile).toList();
  }

  private ClassMethodCallResult analyzeByteCodeFile(Path byteCodeFile) {
    try (InputStream inputStream = Files.newInputStream(byteCodeFile)) {
      ClassReader classReader = new ClassReader(inputStream);
      CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
      classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

      return ClassMethodCallResult.of(
          classReader.getClassName(),
          classReader.getInterfaces(),
          callGraphClassVisitor.getMethodCalls());
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
