package com.opensourcereader.core.analysis.domain.service.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeStructureWithMethod;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeWithMethodCall;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.factory.OpenSourceRepoFactory;
import com.opensourcereader.core.analysis.domain.entity.factory.ExternalTypeStructureFactory;
import com.opensourcereader.core.analysis.domain.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.domain.service.hierarchy.InheritanceLinker;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.testfixture.TestTypeFixtures;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MethodCallResolverTest {

  InheritanceMethodDispatcher inheritanceMethodDispatcher = new InheritanceMethodDispatcher();
  InterfaceMethodDispatcher interfaceMethodDispatcher =
      new InterfaceMethodDispatcher(new TypeGraphValidator());
  MethodCallResolver methodCallResolver = new MethodCallResolver();
  OpenSourceRepoFactory openSourceRepoFactory =
      new OpenSourceRepoFactory(new InheritanceLinker(), new ExternalTypeStructureFactory());

  @Test
  @DisplayName("Callee 메서드가 존재하면 → Caller DeclaredMethod와 연결된다")
  void givenExistingCalleeMethod_whenCreate_thenLinkResolvedMethod() {
    // given
    String calleeClassName = "b/B";
    String calleeMethodName = "b";
    MethodDescriptor calleeMethodDescriptor = MethodDescriptor.from("()V");
    TypeStructure calleeTypeStructure =
        createTypeStructureWithMethod(
            calleeClassName, null, null, calleeMethodName, calleeMethodDescriptor, TypeKind.CLASS);

    String callerClassName = "a/A";
    String callerMethodName = "a";
    MethodDescriptor callerMethodDescriptor = MethodDescriptor.from("()V");
    TypeStructure callerTypeStructure =
        createTypeWithMethodCall(
            TypeKind.CLASS,
            callerClassName,
            callerMethodName,
            callerMethodDescriptor,
            calleeClassName,
            calleeMethodName,
            calleeMethodDescriptor);
    OpenSourceRepo openSourceRepo =
        openSourceRepoFactory.create(
            "new-cloneUrl", List.of(callerTypeStructure, calleeTypeStructure));

    // when
    methodCallResolver.create(
        openSourceRepo.getTypes(), List.of(calleeTypeStructure, callerTypeStructure));

    // then
    List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(openSourceRepo.getTypes());
    assertThat(outgoingCalls)
        .extracting(
            e -> e.getCaller().getTypeInternalName(),
            e -> e.getCaller().getMethodName(),
            e -> e.getCallee().getTypeInternalName(),
            e -> e.getCallee().getMethodName())
        .contains(tuple(callerClassName, callerMethodName, calleeClassName, calleeMethodName));
  }

  @DisplayName("method call 기반으로 없는 메서드는 external inheritance로 생성되고 연결된다")
  @Test
  void givenMissingMethodCall_whenResolve_thenCreateExternalInheritanceMethod() {
    // given
    String targetTypeName = "A";
    String methodName = "foo";
    MethodDescriptor descriptor = MethodDescriptor.from("()V");

    // A 클래스는 아무 메서드도 선언하지 않음
    TypeStructure type =
        TestTypeFixtures.createTypeStructure(targetTypeName, null, null, TypeKind.CLASS);

    // 다른 클래스에서 A.foo() 호출이 존재
    MethodCallInfo callInfo = new MethodCallInfo(9, targetTypeName, methodName, descriptor, false);
    String callerTypeName = "B";
    TypeStructure callerType =
        TestTypeFixtures.createTypeStructureWithCallee(
            callerTypeName,
            null,
            null,
            "caller",
            MethodDescriptor.from("()V"),
            List.of(callInfo),
            TypeKind.CLASS);

    OpenSourceRepo repo = openSourceRepoFactory.create("cloneUrl", List.of(type, callerType));

    // 상속/인터페이스 먼저 연결 (내부 world 완성 단계)
    interfaceMethodDispatcher.connectInterfaceImplementations(repo.getTypes());
    inheritanceMethodDispatcher.connectInheritance(repo.getTypes());

    // when
    methodCallResolver.create(repo.getTypes(), List.of(type, callerType));

    // then
    assertThat(repo.getTypes().stream().flatMap(type1 -> type1.getMethods().stream()).toList())
        .hasSize(2);
    List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
    assertThat(outgoingCalls)
        .extracting(
            e -> e.getCallee().getTypeInternalName(),
            e -> e.getCallee().getMethodName(),
            e -> e.getCallee().getOrigin())
        .contains(Tuple.tuple(targetTypeName, methodName, MethodOrigin.EXTERNAL_INHERITED_VIRTUAL));
  }

  @DisplayName("이미 존재하는 메서드는 external inheritance로 생성되지 않는다")
  @Test
  void givenMethodAlreadyExists_whenResolve_thenDoNotCreateExternalInheritance() {
    // given
    String targetTypeName = "A";
    String methodName = "foo";
    MethodDescriptor descriptor = MethodDescriptor.from("()V");

    TypeStructure type =
        TestTypeFixtures.createTypeStructureWithMethod(
            targetTypeName, null, null, methodName, descriptor, TypeKind.CLASS);

    // 같은 foo() 호출 존재
    String callerTypeName = "B";
    MethodCallInfo callInfo = new MethodCallInfo(9, targetTypeName, methodName, descriptor, false);
    TypeStructure callerType =
        TestTypeFixtures.createTypeStructureWithCallee(
            callerTypeName,
            null,
            null,
            "caller",
            MethodDescriptor.from("()V"),
            List.of(callInfo),
            TypeKind.CLASS);

    OpenSourceRepo repo = openSourceRepoFactory.create("cloneUrl", List.of(type, callerType));

    interfaceMethodDispatcher.connectInterfaceImplementations(repo.getTypes());
    inheritanceMethodDispatcher.connectInheritance(repo.getTypes());

    // when
    methodCallResolver.create(repo.getTypes(), List.of(type, callerType));

    // then
    assertThat(repo.getTypes().stream().flatMap(type1 -> type1.getMethods().stream()).toList())
        .hasSize(2);
    assertThat(
            repo.getTypes().stream()
                .filter(t -> t.getTypeInternalName().equals(targetTypeName))
                .flatMap(type1 -> type1.getMethods().stream())
                .toList())
        .extracting(Method::getMethodSignature, Method::getOrigin)
        .contains(Tuple.tuple(MethodSignature.from(callInfo), MethodOrigin.INTERNAL_DECLARED));
  }
}
