package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import com.opensourcereader.core.analysis.dto.CalleePathAndMethodDescriptor;

import lombok.Getter;

@Getter
public class CallGraphClassVisitor extends ClassVisitor {

  private final Map<String, Set<CalleePathAndMethodDescriptor>> graph = new HashMap<>();

  public CallGraphClassVisitor() {
    super(Opcodes.ASM9);
  }

  @Override
  public MethodVisitor visitMethod(
      int access, String name, String descriptor, String signature, String[] exceptions) {
    String callerMethodSignature = name + getArgumentTypes(descriptor);
    return new MethodVisitor(Opcodes.ASM9) {

      @Override
      public void visitMethodInsn(
          int opcode,
          String owner,
          String calleeName,
          String calleeDescription,
          boolean isInterface) {

        graph
            .computeIfAbsent(callerMethodSignature, k -> new HashSet<>())
            .add(
                CalleePathAndMethodDescriptor.of(
                    owner, calleeName + getArgumentTypes(calleeDescription)));
      }
    };
  }

  private String getArgumentTypes(String descriptor) {
    String paramsPart = descriptor.substring(descriptor.indexOf('(') + 1, descriptor.indexOf(')'));
    String[] paramsTypeTokens = paramsPart.split(";");
    StringJoiner joiner = new StringJoiner(".");
    for (String typeToken : paramsTypeTokens) {
      String substring = typeToken.substring(typeToken.lastIndexOf('/') + 1);
      joiner.add(substring);
    }
    return joiner.toString();
  }
}
