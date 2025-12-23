package com.opensourcereader.core.analysis.service.impl.callgraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;

import lombok.Getter;

@Getter
public class CallGraphClassVisitor extends ClassVisitor {

  private final Map<String, Set<CalleePathAndMethodDescriptor>> graph = new HashMap<>();
  private String currentClass;

  public CallGraphClassVisitor() {
    super(Opcodes.ASM9);
  }

  @Override
  public void visit(
      int version,
      int access,
      String name,
      String signature,
      String superName,
      String[] interfaces) {
    this.currentClass = name;
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

        CalleePathAndMethodDescriptor calleePathAndMethodDescriptor =
            new CalleePathAndMethodDescriptor(
                owner, calleeName + getArgumentTypes(calleeDescription));
        graph
            .computeIfAbsent(callerMethodSignature, k -> new HashSet<>())
            .add(calleePathAndMethodDescriptor);
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

  public record CalleePathAndMethodDescriptor(String calleePath, String methodDescriptor) {}
}
