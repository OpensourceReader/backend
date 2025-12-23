package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import aj.org.objectweb.asm.ClassReader;
import com.opensourcereader.core.analysis.service.impl.callgraph.CallGraphClassVisitor.CalleePathAndMethodDescriptor;

@Component
public class CallGraphAnalyzer {

  public CallGraphResult analyzeByteCodeFile(Path byteCodeFile) {
    try (InputStream inputStream = Files.newInputStream(byteCodeFile)) {
      ClassReader classReader = new ClassReader(inputStream);
      String internalName = classReader.getClassName();
      String[] interfaces = classReader.getInterfaces();
      CallGraphClassVisitor callGraphClassVisitor = new CallGraphClassVisitor();
      classReader.accept(callGraphClassVisitor, ClassReader.SKIP_DEBUG);

      return CallGraphResult.of(internalName, interfaces, callGraphClassVisitor.getGraph());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public record CallGraphResult(
      String classInternalName,
      List<String> interfaces,
      Map<String, Set<CalleePathAndMethodDescriptor>> edges) {

    public static CallGraphResult of(
        String classInternalName,
        String[] interfaces,
        Map<String, Set<CalleePathAndMethodDescriptor>> edges) {
      return new CallGraphResult(classInternalName, Arrays.stream(interfaces).toList(), edges);
    }
  }
}
