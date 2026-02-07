package com.opensourcereader.core.analysis.infra.bytecode;

import java.util.ArrayList;
import java.util.List;

import aj.org.objectweb.asm.ClassVisitor;
import aj.org.objectweb.asm.MethodVisitor;
import aj.org.objectweb.asm.Opcodes;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeDeclaredMethodInfo;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeMethodStructure;

import lombok.Getter;

@Getter
public class ClassStructureVisitor extends ClassVisitor {

  private int classAccess;
  private TypeInfo typeInfo;
  private final List<ByteCodeMethodStructure> byteCodeMethodStructures = new ArrayList<>();

  public ClassStructureVisitor() {
    super(Opcodes.ASM9);
  }

  @Override
  public void visit(
      int version,
      int classAccess,
      String className,
      String signature,
      String superName,
      String[] interfaces) {
    this.classAccess = classAccess;
    this.typeInfo = TypeInfo.of(version, classAccess, className, signature, superName, interfaces);
  }

  @Override
  public MethodVisitor visitMethod(
      int methodAccess,
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
        ByteCodeDeclaredMethodInfo byteCodeDeclaredMethodInfo =
            ByteCodeDeclaredMethodInfo.of(
                typeInfo.typeInternalName(),
                classAccess,
                methodAccess,
                callerMethodName,
                callerDescriptor,
                genericSignature,
                exceptions);
        byteCodeMethodStructures.add(
            new ByteCodeMethodStructure(byteCodeDeclaredMethodInfo, methodCalls));
      }
    };
  }
}
