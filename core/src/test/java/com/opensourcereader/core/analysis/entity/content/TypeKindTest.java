package com.opensourcereader.core.analysis.entity.content;

import static org.assertj.core.api.Assertions.assertThat;

import aj.org.objectweb.asm.Opcodes;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TypeKindTest {

  @Test
  @DisplayName("CLASS: 어떤 특수 플래그도 없으면 CLASS")
  void class_type() {
    int classAccess = Opcodes.ACC_PUBLIC;

    TypeKind kind = TypeKind.from(classAccess);

    assertThat(kind).isEqualTo(TypeKind.CLASS);
  }

  @Test
  @DisplayName("INTERFACE: ACC_INTERFACE가 있으면 INTERFACE")
  void interface_type() {
    int classAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE;

    TypeKind kind = TypeKind.from(classAccess);

    assertThat(kind).isEqualTo(TypeKind.INTERFACE);
  }

  @Test
  @DisplayName("ANNOTATION: annotation은 ACC_INTERFACE + ACC_ANNOTATION")
  void annotation_type() {
    int classAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION;

    TypeKind kind = TypeKind.from(classAccess);

    assertThat(kind).isEqualTo(TypeKind.ANNOTATION);
  }

  @Test
  @DisplayName("ENUM: ACC_ENUM이 있으면 ENUM")
  void enum_type() {
    int classAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_ENUM;

    TypeKind kind = TypeKind.from(classAccess);

    assertThat(kind).isEqualTo(TypeKind.ENUM);
  }

  @Test
  @DisplayName("RECORD: ACC_RECORD가 있으면 RECORD")
  void record_type() {
    int classAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_RECORD;

    TypeKind kind = TypeKind.from(classAccess);

    assertThat(kind).isEqualTo(TypeKind.RECORD);
  }

  @Test
  @DisplayName("우선순위: ANNOTATION은 INTERFACE보다 우선한다")
  void annotation_has_higher_priority_than_interface() {
    int classAccess = Opcodes.ACC_INTERFACE | Opcodes.ACC_ANNOTATION;

    TypeKind kind = TypeKind.from(classAccess);

    assertThat(kind).isEqualTo(TypeKind.ANNOTATION);
  }
}
