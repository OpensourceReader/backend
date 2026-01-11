package com.opensourcereader.core.analysis.entity.method;

import java.util.EnumSet;

import aj.org.objectweb.asm.Opcodes;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;

public enum MethodModifier {
  // Access modifiers (exactly one)
  PRIVATE,
  PUBLIC,
  PROTECTED,
  PACKAGE_PRIVATE,

  // Non-access modifiers
  STATIC,
  FINAL,
  ABSTRACT,
  SYNCHRONIZED,
  NATIVE, // 소스 코드에 선언은 있으나 실제 구현은 네이티브 라이브러리
  STRICTFP, // 부동 소수점 계산 규칙 고정
  INTERFACE_DEFAULT,
  BRIDGE, // 소스 파일에는 없는 메서드
  SYNTHETIC, // 소스 파일에는 없는 메서드
  VARARGS; // 가변 인자를 받는 메서드

  // DEFAULT, BRIDGE, SYNTHETIC, VARARGS는 소스 레벨에서는 알 수 없음
  public static EnumSet<MethodModifier> from(MethodDeclaration md) {
    EnumSet<MethodModifier> mods = EnumSet.noneOf(MethodModifier.class);

    if (md.hasModifier(Modifier.Keyword.PUBLIC)) {
      mods.add(MethodModifier.PUBLIC);
    } else if (md.hasModifier(Modifier.Keyword.PROTECTED)) {
      mods.add(MethodModifier.PROTECTED);
    } else if (md.hasModifier(Modifier.Keyword.PRIVATE)) {
      mods.add(MethodModifier.PRIVATE);
    } else {
      mods.add(MethodModifier.PACKAGE_PRIVATE);
    }

    if (md.hasModifier(Modifier.Keyword.STATIC)) {
      mods.add(MethodModifier.STATIC);
    }
    if (md.hasModifier(Modifier.Keyword.FINAL)) {
      mods.add(MethodModifier.FINAL);
    }
    if (md.hasModifier(Modifier.Keyword.ABSTRACT)) {
      mods.add(MethodModifier.ABSTRACT);
    }
    if (md.hasModifier(Modifier.Keyword.SYNCHRONIZED)) {
      mods.add(MethodModifier.SYNCHRONIZED);
    }
    if (md.hasModifier(Modifier.Keyword.NATIVE)) {
      mods.add(MethodModifier.NATIVE);
    }
    if (md.hasModifier(Modifier.Keyword.STRICTFP)) {
      mods.add(MethodModifier.STRICTFP);
    }

    return mods;
  }

  // default 메서드는 메서드만 보고 결정할 수 없어서, classAccess 필요
  public static EnumSet<MethodModifier> from(int classAccess, int methodAccess) {
    EnumSet<MethodModifier> mods = EnumSet.noneOf(MethodModifier.class);

    /* =========================
     * 1) Access modifier (exactly one)
     * ========================= */
    if ((methodAccess & Opcodes.ACC_PUBLIC) != 0) {
      mods.add(PUBLIC);
    } else if ((methodAccess & Opcodes.ACC_PROTECTED) != 0) {
      mods.add(PROTECTED);
    } else if ((methodAccess & Opcodes.ACC_PRIVATE) != 0) {
      mods.add(PRIVATE);
    } else {
      mods.add(PACKAGE_PRIVATE);
    }

    /* =========================
     * 2) Non-access modifiers
     * ========================= */
    if ((methodAccess & Opcodes.ACC_STATIC) != 0) {
      mods.add(STATIC);
    }
    if ((methodAccess & Opcodes.ACC_FINAL) != 0) {
      mods.add(FINAL);
    }
    if ((methodAccess & Opcodes.ACC_ABSTRACT) != 0) {
      mods.add(ABSTRACT);
    }
    if ((methodAccess & Opcodes.ACC_SYNCHRONIZED) != 0) {
      mods.add(SYNCHRONIZED);
    }
    if ((methodAccess & Opcodes.ACC_NATIVE) != 0) {
      mods.add(NATIVE);
    }
    if ((methodAccess & Opcodes.ACC_STRICT) != 0) {
      mods.add(STRICTFP);
    }
    if ((methodAccess & Opcodes.ACC_BRIDGE) != 0) {
      mods.add(BRIDGE);
    }
    if ((methodAccess & Opcodes.ACC_SYNTHETIC) != 0) {
      mods.add(SYNTHETIC);
    }
    if ((methodAccess & Opcodes.ACC_VARARGS) != 0) {
      mods.add(VARARGS);
    }

    /* =========================
     * 3) DEFAULT method inference
     * ========================= */
    boolean isInterface = (classAccess & Opcodes.ACC_INTERFACE) != 0;

    if (isInterface
        && !mods.contains(ABSTRACT)
        && !mods.contains(STATIC)
        && !mods.contains(PRIVATE)) {
      mods.add(INTERFACE_DEFAULT);
    }

    return mods;
  }

  public static EnumSet<MethodModifier> from(int methodAccess) {
    return from(0, methodAccess); // DEFAULT inference disabled
  }

  public static MethodModifier accessFrom(int methodAccess) {
    if ((methodAccess & Opcodes.ACC_PUBLIC) != 0) {
      return PUBLIC;
    }
    if ((methodAccess & Opcodes.ACC_PROTECTED) != 0) {
      return PROTECTED;
    }
    if ((methodAccess & Opcodes.ACC_PRIVATE) != 0) {
      return PRIVATE;
    }
    return PACKAGE_PRIVATE;
  }
}
