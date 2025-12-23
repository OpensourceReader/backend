package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.dto.callgraph.CallGraphResult;

@Component
public class CallGraphAnalyzer {

  public CallGraphResult analyzeByteCodeFile(Path byteCodeFile) {
    try (InputStream inputStream = Files.newInputStream(byteCodeFile)) {
      ClassReader classReader = new ClassReader(inputStream);
      CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
      classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

      return CallGraphResult.of(
          classReader.getClassName(),
          classReader.getInterfaces(),
          callGraphClassVisitor.getRawMethodCalls());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
