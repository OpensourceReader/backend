package com.opensourcereader.core.analysis.entity.codemethod;

import java.util.EnumSet;

import aj.org.objectweb.asm.Opcodes;

public enum NonAccessModifier {
  STATIC,
  FINAL,
  ABSTRACT,
  SYNCHRONIZED,
  NATIVE,
  STRICTFP,
  DEFAULT,
  BRIDGE,
  SYNTHETIC,
  VARARGS;

  public static EnumSet<NonAccessModifier> from(int access) {
    EnumSet<NonAccessModifier> mods = EnumSet.noneOf(NonAccessModifier.class);

    if ((access & Opcodes.ACC_STATIC) != 0) {
      mods.add(NonAccessModifier.STATIC);
    }
    if ((access & Opcodes.ACC_FINAL) != 0) {
      mods.add(NonAccessModifier.FINAL);
    }
    if ((access & Opcodes.ACC_ABSTRACT) != 0) {
      mods.add(NonAccessModifier.ABSTRACT);
    }
    if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
      mods.add(NonAccessModifier.SYNCHRONIZED);
    }
    if ((access & Opcodes.ACC_NATIVE) != 0) {
      mods.add(NonAccessModifier.NATIVE);
    }
    if ((access & Opcodes.ACC_STRICT) != 0) {
      mods.add(NonAccessModifier.STRICTFP);
    }

    if ((access & Opcodes.ACC_BRIDGE) != 0) {
      mods.add(NonAccessModifier.BRIDGE);
    }
    if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      mods.add(NonAccessModifier.SYNTHETIC);
    }
    if ((access & Opcodes.ACC_VARARGS) != 0) {
      mods.add(NonAccessModifier.VARARGS);
    }

    return mods;
  }
}
