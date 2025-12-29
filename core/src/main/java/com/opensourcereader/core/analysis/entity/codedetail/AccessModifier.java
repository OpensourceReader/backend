package com.opensourcereader.core.analysis.entity.codedetail;

import aj.org.objectweb.asm.Opcodes;
import com.github.javaparser.ast.body.MethodDeclaration;

public enum AccessModifier {
  PRIVATE,
  PUBLIC,
  PROTECTED,
  PACKAGE_PRIVATE;

  public static AccessModifier from(MethodDeclaration methodDeclaration) {
    if (methodDeclaration.isPublic()) {
      return PUBLIC;
    }
    if (methodDeclaration.isPrivate()) {
      return PRIVATE;
    }
    if (methodDeclaration.isProtected()) {
      return PROTECTED;
    }
    return PACKAGE_PRIVATE;
  }

  public static AccessModifier from(int access) {
    if ((access & Opcodes.ACC_PUBLIC) != 0) return AccessModifier.PUBLIC;
    if ((access & Opcodes.ACC_PROTECTED) != 0) return AccessModifier.PROTECTED;
    if ((access & Opcodes.ACC_PRIVATE) != 0) return AccessModifier.PRIVATE;
    return AccessModifier.PACKAGE_PRIVATE;
  }
}
