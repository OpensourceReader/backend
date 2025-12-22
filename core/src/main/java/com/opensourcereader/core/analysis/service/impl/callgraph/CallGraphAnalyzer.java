package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphClassVisitor.CalleePathAndMethodDescriptor;

@Service
public class CallGraphAnalyzer {

  public CallGraphResult analyzeByteCodeFile(Path byteCodeFile) {
    try (InputStream inputStream = Files.newInputStream(byteCodeFile)) {
      ClassReader classReader = new ClassReader(inputStream);
      String internalName = classReader.getClassName();
      CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
      classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

      return new CallGraphResult(internalName, callGraphClassVisitor.getGraph());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public record CallGraphResult(
      String classInternalName, Map<String, Set<CalleePathAndMethodDescriptor>> edges) {}
}
