package com.opensourcereader.core.analysis.entity.method;

import java.util.EnumSet;

import aj.org.objectweb.asm.Opcodes;

public enum MethodModifier {
  STATIC,
  FINAL,
  ABSTRACT,
  SYNCHRONIZED,
  NATIVE,
  STRICTFP,
  DEFAULT, // inferred (no direct flag)
  BRIDGE,
  SYNTHETIC,
  VARARGS;

  public static EnumSet<MethodModifier> from(int classAccess, int methodAccess) {
    EnumSet<MethodModifier> mods = EnumSet.noneOf(MethodModifier.class);

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

    boolean isInterface = (classAccess & Opcodes.ACC_INTERFACE) != 0;
    boolean isAbstract = (methodAccess & Opcodes.ACC_ABSTRACT) != 0;
    boolean isStatic = (methodAccess & Opcodes.ACC_STATIC) != 0;
    boolean isPrivate = (methodAccess & Opcodes.ACC_PRIVATE) != 0;

    if (isInterface && !isAbstract && !isStatic && !isPrivate) {
      mods.add(DEFAULT);
    }

    return mods;
  }

  public static EnumSet<MethodModifier> from(int methodAccess) {
    return from(0, methodAccess); // classAccess=0 => isInterface=false => DEFAULT won't be added
  }
}
