package com.opensourcereader.core.analysis.entity.repo;

import aj.org.objectweb.asm.Opcodes;

public enum TypeKind {
  CLASS,
  INTERFACE,
  ENUM,
  RECORD,
  ANNOTATION;

  public static TypeKind fromClassAccess(int classAccess) {
    // 1) Annotation: annotation은 interface 플래그도 같이 켜지므로 먼저 체크
    if ((classAccess & Opcodes.ACC_ANNOTATION) != 0) {
      return TypeKind.ANNOTATION;
    }

    // 2) Enum
    if ((classAccess & Opcodes.ACC_ENUM) != 0) {
      return TypeKind.ENUM;
    }

    // 3) Record (ASM이 ACC_RECORD를 지원할 때)
    // 주의: 사용 중인 ASM 버전에 따라 Opcodes.ACC_RECORD가 없을 수 있음
    if ((classAccess & Opcodes.ACC_RECORD) != 0) {
      return TypeKind.RECORD;
    }

    // 4) Interface
    if ((classAccess & Opcodes.ACC_INTERFACE) != 0) {
      return TypeKind.INTERFACE;
    }

    // 5) Default
    return TypeKind.CLASS;
  }
}
