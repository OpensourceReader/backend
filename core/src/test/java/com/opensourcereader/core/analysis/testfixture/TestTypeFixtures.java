package com.opensourcereader.core.analysis.testfixture;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.file.RepoEntryType;
import com.opensourcereader.core.analysis.domain.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;

public final class TestTypeFixtures {

  private TestTypeFixtures() {}

  private static MethodInfo createMethodInfo(
      String typeInternalName, String methodName, MethodDescriptor methodDescriptor) {
    return new MethodInfo(
        typeInternalName,
        methodName,
        EnumSet.of(MethodModifier.PUBLIC),
        methodDescriptor,
        null,
        null,
        1,
        1);
  }

  public static TypeStructure createTypeStructureWithMethod(
      String typeInternalName,
      String superName,
      List<String> interfaceNames,
      String methodName,
      MethodDescriptor methodDescriptor,
      TypeKind typeKind) {
    if (interfaceNames == null) {
      interfaceNames = List.of();
    }

    TypeInfo typeInfo =
        new TypeInfo(33, typeKind, typeInternalName, null, superName, interfaceNames);

    return new TypeStructure(
        typeInternalName + ".path",
        RepoEntryType.FILE,
        null,
        typeInfo,
        List.of(
            new MethodStructure(
                createMethodInfo(typeInternalName, methodName, methodDescriptor), List.of())));
  }

  public static TypeStructure createTypeStructureWithoutMethod(
      String typeInternalName, TypeKind typeKind, String superName, List<String> interfaceNames) {
    if (interfaceNames == null) {
      interfaceNames = List.of();
    }
    TypeInfo typeInfo =
        new TypeInfo(33, typeKind, typeInternalName, null, superName, interfaceNames);

    return new TypeStructure(
        typeInternalName + ".path", RepoEntryType.FILE, null, typeInfo, List.of());
  }

  public static TypeStructure createTypeWithMethodCall(
      TypeKind typeKind,
      String callerTypeInternalName,
      String callerMethodName,
      MethodDescriptor callerMethodDescriptor,
      String calleeTypeInternalName,
      String calleeMethodName,
      MethodDescriptor calleeMethodDescriptor) {
    TypeInfo callerTypeInfo =
        new TypeInfo(33, typeKind, callerTypeInternalName, null, null, List.of());

    return new TypeStructure(
        callerTypeInternalName + "path",
        RepoEntryType.FILE,
        null,
        callerTypeInfo,
        List.of(
            new MethodStructure(
                createMethodInfo(callerTypeInternalName, callerMethodName, callerMethodDescriptor),
                List.of(
                    new MethodCallInfo(
                        9,
                        calleeTypeInternalName,
                        calleeMethodName,
                        calleeMethodDescriptor,
                        false)))));
  }

  public static TypeStructure createTypeStructure(
      String typeInternalName, String superName, List<String> interfaceNames, TypeKind typeKind) {
    if (interfaceNames == null) {
      interfaceNames = List.of();
    }

    TypeInfo typeInfo =
        new TypeInfo(33, typeKind, typeInternalName, null, superName, interfaceNames);

    return new TypeStructure(
        typeInternalName + ".path", RepoEntryType.FILE, null, typeInfo, List.of());
  }

  public static TypeStructure createTypeStructureWithCallee(
      String typeInternalName,
      String superName,
      List<String> interfaceNames,
      String callerMethodName,
      MethodDescriptor callerMethodDescriptor,
      List<MethodCallInfo> callees,
      TypeKind typeKind) {
    if (interfaceNames == null) {
      interfaceNames = List.of();
    }

    TypeInfo typeInfo =
        new TypeInfo(33, typeKind, typeInternalName, null, superName, interfaceNames);

    return new TypeStructure(
        typeInternalName + ".path",
        RepoEntryType.FILE,
        null,
        typeInfo,
        List.of(
            new MethodStructure(
                createMethodInfo(typeInternalName, callerMethodName, callerMethodDescriptor),
                callees)));
  }
}
