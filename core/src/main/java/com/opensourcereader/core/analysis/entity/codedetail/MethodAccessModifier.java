package com.opensourcereader.core.analysis.entity.codedetail;

import com.github.javaparser.ast.body.MethodDeclaration;

public enum MethodAccessModifier {
  PRIVATE,
  PUBLIC,
  PROTECTED,
  PACKAGE_PRIVATE;

  public static MethodAccessModifier from(MethodDeclaration methodDeclaration) {
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
}
