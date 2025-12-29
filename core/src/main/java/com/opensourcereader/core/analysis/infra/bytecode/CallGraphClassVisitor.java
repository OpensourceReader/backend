package com.opensourcereader.core.analysis.infra.bytecode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodStructure;

import lombok.Getter;

@Getter
public class CallGraphClassVisitor extends ClassVisitor {

  private ClassInfo classInfo;
  private final List<MethodStructure> methodStructures = new ArrayList<>();

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
    this.classInfo =
        new ClassInfo(
            version, access, className, signature, superName, Arrays.stream(interfaces).toList());
  }

  @Override
  public MethodVisitor visitMethod(
      int access,
      String callerMethodName,
      String callerDescriptor,
      String genericSignature,
      String[] exceptions) {
    return new MethodVisitor(Opcodes.ASM9) {
      List<MethodCallInfo> methodCalls = new ArrayList<>();

      @Override
      public void visitMethodInsn(
          int opcode,
          String calleeOwnerClassName,
          String calleeMethodName,
          String calleeDescription,
          boolean isInterface) {
        MethodCallInfo methodCallInfo =
            MethodCallInfo.of(
                opcode, calleeOwnerClassName, calleeMethodName, calleeDescription, isInterface);
        methodCalls.add(methodCallInfo);
      }

      @Override
      public void visitEnd() {
        DeclaredMethodInfo declaredMethodInfo =
            DeclaredMethodInfo.of(
                classInfo.className(),
                access,
                callerMethodName,
                callerDescriptor,
                genericSignature,
                exceptions);
        methodStructures.add(new MethodStructure(declaredMethodInfo, methodCalls));
      }
    };
  }
}
