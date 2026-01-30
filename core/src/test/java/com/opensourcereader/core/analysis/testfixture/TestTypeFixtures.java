package com.opensourcereader.core.analysis.testfixture;

import java.util.EnumSet;
import java.util.List;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta.MethodCallInfo;
import com.opensourcereader.core.analysis.entity.shared.MethodModifier;
import com.opensourcereader.core.analysis.entity.shared.RepoEntryType;
import com.opensourcereader.core.analysis.entity.shared.TypeKind;

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
      String filePath,
      RepoEntryType repoEntryType,
      String callerTypeInternalName,
      String callerMethodName,
      MethodDescriptor callerMethodDescriptor,
      String calleeTypeInternalName,
      String calleeMethodName,
      MethodDescriptor calleeMethodDescriptor,
      boolean isInterface) {
    TypeInfo callerTypeInfo =
        new TypeInfo(33, TypeKind.CLASS, callerTypeInternalName, null, null, List.of());

    return new TypeStructure(
        filePath,
        repoEntryType,
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
                        isInterface)))));
  }

  private static DeclaredMethodInfo createMethodInfo(
      String typeInternalName, String methodName, MethodDescriptor methodDescriptor) {
    return new DeclaredMethodInfo(
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
