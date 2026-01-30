package com.opensourcereader.core.analysis.entity.method;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.core.analysis.entity.shared.MethodModifier;
import java.util.EnumSet;

import aj.org.objectweb.asm.Opcodes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MethodModifierTest {

  @Test
  @DisplayName("Access: public이면 PUBLIC 포함, 다른 access는 없음")
  void access_public() {
    EnumSet<MethodModifier> mods = MethodModifier.from(0, Opcodes.ACC_PUBLIC);

    assertThat(mods).contains(MethodModifier.PUBLIC);
    assertThat(mods)
        .doesNotContain(
            MethodModifier.PRIVATE, MethodModifier.PROTECTED, MethodModifier.PACKAGE_PRIVATE);
  }

  @Test
  @DisplayName("Access: 아무 access flag 없으면 PACKAGE_PRIVATE 포함")
  void access_package_private_when_no_access_flag() {
    EnumSet<MethodModifier> mods = MethodModifier.from(0, 0);

    assertThat(mods).contains(MethodModifier.PACKAGE_PRIVATE);
    assertThat(mods)
        .doesNotContain(MethodModifier.PUBLIC, MethodModifier.PROTECTED, MethodModifier.PRIVATE);
  }

  @Test
  @DisplayName("Non-access: public static final이면 PUBLIC, STATIC, FINAL 포함")
  void public_static_final() {
    int methodAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;

    EnumSet<MethodModifier> mods = MethodModifier.from(0, methodAccess);

    assertThat(mods)
        .containsExactlyInAnyOrder(
            MethodModifier.PUBLIC, MethodModifier.STATIC, MethodModifier.FINAL);
  }

  @Test
  @DisplayName(
      "Non-access: protected abstract synchronized native strictfp varargs bridge synthetic 플래그가 모두 set에 들어간다")
  void multiple_flags_all_included() {
    int methodAccess =
        Opcodes.ACC_PROTECTED
            | Opcodes.ACC_ABSTRACT
            | Opcodes.ACC_SYNCHRONIZED
            | Opcodes.ACC_NATIVE
            | Opcodes.ACC_STRICT
            | Opcodes.ACC_VARARGS
            | Opcodes.ACC_BRIDGE
            | Opcodes.ACC_SYNTHETIC;

    EnumSet<MethodModifier> mods = MethodModifier.from(0, methodAccess);

    assertThat(mods)
        .contains(
            MethodModifier.PROTECTED,
            MethodModifier.ABSTRACT,
            MethodModifier.SYNCHRONIZED,
            MethodModifier.NATIVE,
            MethodModifier.STRICTFP,
            MethodModifier.VARARGS,
            MethodModifier.BRIDGE,
            MethodModifier.SYNTHETIC);

    // access는 PROTECTED 하나만
    assertThat(mods)
        .doesNotContain(
            MethodModifier.PUBLIC, MethodModifier.PRIVATE, MethodModifier.PACKAGE_PRIVATE);
  }

  @Test
  @DisplayName("DEFAULT 추론: interface + non-abstract + non-static + non-private면 DEFAULT가 추가된다")
  void infer_default_method() {
    int classAccess = Opcodes.ACC_INTERFACE;
    int methodAccess = Opcodes.ACC_PUBLIC; // abstract 아님, static 아님, private 아님

    EnumSet<MethodModifier> mods = MethodModifier.from(classAccess, methodAccess);

    assertThat(mods).contains(MethodModifier.PUBLIC, MethodModifier.INTERFACE_DEFAULT);
    assertThat(mods)
        .doesNotContain(MethodModifier.ABSTRACT, MethodModifier.STATIC, MethodModifier.PRIVATE);
  }

  @Test
  @DisplayName("DEFAULT 추론 안함: interface + abstract면 DEFAULT가 추가되지 않는다")
  void no_default_when_abstract() {
    int classAccess = Opcodes.ACC_INTERFACE;
    int methodAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT;

    EnumSet<MethodModifier> mods = MethodModifier.from(classAccess, methodAccess);

    assertThat(mods).contains(MethodModifier.PUBLIC, MethodModifier.ABSTRACT);
    assertThat(mods).doesNotContain(MethodModifier.INTERFACE_DEFAULT);
  }

  @Test
  @DisplayName("DEFAULT 추론 안함: interface + static이면 DEFAULT가 추가되지 않는다")
  void no_default_when_static() {
    int classAccess = Opcodes.ACC_INTERFACE;
    int methodAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;

    EnumSet<MethodModifier> mods = MethodModifier.from(classAccess, methodAccess);

    assertThat(mods).contains(MethodModifier.PUBLIC, MethodModifier.STATIC);
    assertThat(mods).doesNotContain(MethodModifier.INTERFACE_DEFAULT);
  }

  @Test
  @DisplayName("DEFAULT 추론 안함: interface + private이면 DEFAULT가 추가되지 않는다")
  void no_default_when_private() {
    int classAccess = Opcodes.ACC_INTERFACE;
    int methodAccess = Opcodes.ACC_PRIVATE; // private method in interface (Java 9+)

    EnumSet<MethodModifier> mods = MethodModifier.from(classAccess, methodAccess);

    assertThat(mods).contains(MethodModifier.PRIVATE);
    assertThat(mods).doesNotContain(MethodModifier.INTERFACE_DEFAULT);
  }

  @Test
  @DisplayName("DEFAULT 추론 비활성: from(methodAccess)에서는 DEFAULT를 추론하지 않는다")
  void default_inference_disabled_when_class_access_unknown() {
    int methodAccess = Opcodes.ACC_PUBLIC;

    EnumSet<MethodModifier> mods = MethodModifier.from(methodAccess);

    assertThat(mods).contains(MethodModifier.PUBLIC);
    assertThat(mods).doesNotContain(MethodModifier.INTERFACE_DEFAULT);
  }
}
