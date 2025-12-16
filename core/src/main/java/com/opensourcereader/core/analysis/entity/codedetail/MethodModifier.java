package com.opensourcereader.core.analysis.entity.codedetail;

import com.github.javaparser.ast.body.MethodDeclaration;

public enum MethodModifier {
  PRIVATE,
  PUBLIC,
  PROTECTED,
  DEFAULT;

  public static MethodModifier from(MethodDeclaration methodDeclaration) {
    if (methodDeclaration.isPublic()) {
      return PUBLIC;
    }
    if (methodDeclaration.isPrivate()) {
      return PRIVATE;
    }
    if (methodDeclaration.isProtected()) {
      return PROTECTED;
    }
    return DEFAULT;
  }
}
