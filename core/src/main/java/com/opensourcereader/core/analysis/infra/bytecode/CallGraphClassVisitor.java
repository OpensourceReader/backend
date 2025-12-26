package com.opensourcereader.core.analysis.infra.bytecode;

import java.util.ArrayList;
import java.util.List;

import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import com.opensourcereader.core.analysis.dto.callgraph.MethodCallEdge;

import lombok.Getter;

@Getter
public class CallGraphClassVisitor extends ClassVisitor {

  private final List<MethodCallEdge> methodCallEdges = new ArrayList<>();
  private String callerClassName;

  public CallGraphClassVisitor() {
    super(Opcodes.ASM9);
  }

  @Override
  public void visit(
      int version,
      int access,
      String className,
      String signature,
      String superName,
      String[] interfaces) {
    this.callerClassName = className;
  }

  @Override
  public MethodVisitor visitMethod(
      int access,
      String callerMethodName,
      String callerDescriptor,
      String signature,
      String[] exceptions) {
    return new MethodVisitor(Opcodes.ASM9) {
      @Override
      public void visitMethodInsn(
          int opcode,
          String ownerClassName,
          String calleeMethodName,
          String calleeDescription,
          boolean isInterface) {
        MethodCallEdge methodCallEdge =
            MethodCallEdge.of(
                callerClassName,
                callerMethodName,
                callerDescriptor,
                ownerClassName,
                calleeMethodName,
                calleeDescription,
                opcode,
                isInterface);
        methodCallEdges.add(methodCallEdge);
      }
    };
  }
}
