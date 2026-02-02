package com.opensourcereader.core.analysis.testfixture;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.content.RepoEntryType;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.type.TypeKind;

public final class TestTypeFixtures {

  private TestTypeFixtures() {}

  public static TypeStructure createTypeWithMethod(
      String filePath,
      RepoEntryType repoEntryType,
      String typeInternalName,
      String methodName,
      MethodDescriptor methodDescriptor,
      TypeKind typeKind) {
    TypeInfo typeInfo = new TypeInfo(33, typeKind, typeInternalName, null, null, List.of());

    return new TypeStructure(
        filePath,
        repoEntryType,
        null,
        typeInfo,
        List.of(
            new MethodStructure(
                createMethodInfo(typeInternalName, methodName, methodDescriptor), List.of())));
  }

  public static TypeStructure createTypeWithoutMethod(
      String filePath, RepoEntryType repoEntryType, String typeInternalName, TypeKind typeKind) {
    TypeInfo typeInfo = new TypeInfo(33, typeKind, typeInternalName, null, null, List.of());

    return new TypeStructure(filePath, repoEntryType, null, typeInfo, List.of());
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
}
