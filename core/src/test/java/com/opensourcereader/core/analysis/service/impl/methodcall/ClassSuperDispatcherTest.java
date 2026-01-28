package com.opensourcereader.core.analysis.service.impl.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getDeclaredMethodInfo;
import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.DeclaredMethodInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.entity.method.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.DeclaredMethod;
import com.opensourcereader.core.analysis.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.entity.type.DeclaredType;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class ClassSuperDispatcherTest {

  @Autowired private DeclaredTypeRepository declaredTypeRepository;
  @Autowired private ClassSuperDispatcher classSuperDispatcher;

  @Nested
  @DisplayName("1. 클래스 상속")
  class ClassInheritance {

    @Test
    @DisplayName("1-1. override가 없으면 Parent.foo -> Child.foo(virtual)로 연결된다")
    void class_inheritance_no_override_virtual_to_parent() {
      // given
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      // 1) Parent (CLASS): foo()
      String parentName = "Parent";
      ByteCodeClassStructure parentStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
      DeclaredMethodInfo parentDeclaredFoo =
          getDeclaredMethodInfo(parentName, methodName, methodDescriptor);
      DeclaredType parentType =
          DeclaredType.internal(parentStructure.typeInfo(), List.of(parentDeclaredFoo), null);

      // 2) Child (CLASS, extends Parent): foo() 선언 없음
      String childName = "Child";
      ByteCodeClassStructure childStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()), List.of());
      DeclaredType childType = DeclaredType.internal(childStructure.typeInfo(), List.of(), null);

      declaredTypeRepository.saveAll(List.of(parentType, childType));

      // Child extends Parent (너희 프로젝트 extends-edge로 교체)
      childType.update(parentType, List.of());
      declaredTypeRepository.save(childType);

      // when
      List<DeclaredMethod> declaredMethods =
          classSuperDispatcher.dispatchSupers(List.of(parentType, childType));

      // then
      List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(
                  parentName,
                  methodName,
                  childName,
                  methodName,
                  MethodOrigin.INTERNAL_INHERITED_DECLARATION));
    }

    @Test
    @DisplayName("1-2. override가 있으면 Parent.foo -> Child.foo(override) 로 연결된다")
    void class_inheritance_with_override_parent_to_child() {
      // given
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      // 1) Parent (CLASS): foo()
      String parentName = "Parent";
      ByteCodeClassStructure parentStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
      DeclaredMethodInfo parentFoo =
          getDeclaredMethodInfo(parentName, methodName, methodDescriptor);
      DeclaredType parentType =
          DeclaredType.internal(parentStructure.typeInfo(), List.of(parentFoo), null);

      // 2) Child (CLASS, extends Parent): foo() override (직접 선언)
      String childName = "Child";
      ByteCodeClassStructure childStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()), List.of());
      DeclaredMethodInfo childOverrideFoo =
          getDeclaredMethodInfo(childName, methodName, methodDescriptor);
      DeclaredType childType =
          DeclaredType.internal(
              childStructure.typeInfo(), List.of(childOverrideFoo), null); // ✅ override 선언

      declaredTypeRepository.saveAll(List.of(parentType, childType));

      // Child extends Parent (너희 update 시그니처 기준: parentType을 바로 넣는 형태)
      childType.update(parentType, List.of());
      declaredTypeRepository.save(childType);

      // when
      List<DeclaredMethod> declaredMethods =
          classSuperDispatcher.dispatchSupers(List.of(parentType, childType));

      // then
      List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(parentName, methodName, childName, methodName, MethodOrigin.INTERNAL_DECLARED));
    }
  }

  @Nested
  @DisplayName("2. 다단 상속")
  class MultiLevelInheritance {

    @Test
    @DisplayName(
        "2-1. Parent -> Child -> GrandChild 모두 override 없어도, Parent는 Child(Virtual)로 연결, Child는 GrandChild(Virtual)로 연결된다.")
    void chain_no_overrides() {
      // given
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      // Parent: foo()
      String parentName = "Parent";
      ByteCodeClassStructure parentStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
      DeclaredMethodInfo parentFoo =
          getDeclaredMethodInfo(parentName, methodName, methodDescriptor);
      DeclaredType parentType =
          DeclaredType.internal(parentStructure.typeInfo(), List.of(parentFoo), null);

      // Child extends Parent: foo() 없음
      String childName = "Child";
      ByteCodeClassStructure childStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()), List.of());
      DeclaredType childType = DeclaredType.internal(childStructure.typeInfo(), List.of(), null);

      // GrandChild extends Child: foo() 없음
      String grandChildName = "GrandChild";
      ByteCodeClassStructure grandChildStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, grandChildName, childName, null, List.of()),
              List.of());
      DeclaredType grandChildType =
          DeclaredType.internal(grandChildStructure.typeInfo(), List.of(), null);

      declaredTypeRepository.saveAll(List.of(parentType, childType, grandChildType));

      // 타입 그래프 세팅: Child -> Parent, GrandChild -> Child
      childType.update(parentType, List.of());
      declaredTypeRepository.save(childType);

      grandChildType.update(childType, List.of());
      declaredTypeRepository.save(grandChildType);

      // when
      List<DeclaredMethod> declaredMethods =
          classSuperDispatcher.dispatchSupers(List.of(parentType, childType, grandChildType));

      // then
      List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(
                  parentName,
                  methodName,
                  childName,
                  methodName,
                  MethodOrigin.INTERNAL_INHERITED_DECLARATION),
              tuple(
                  childName,
                  methodName,
                  grandChildName,
                  methodName,
                  MethodOrigin.INTERNAL_INHERITED_DECLARATION));
    }

    @Test
    @DisplayName(
        "2-2. 상속 체인에서 중간 클래스(Child)가 override를 하면, 하위 클래스(GrandChild)는 Parent가 아닌 Child를 기준으로 연결된다")
    void chain_middle_override() {
      // given
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      String parentName = "Parent";
      ByteCodeClassStructure parentStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
      DeclaredMethodInfo parentFoo =
          getDeclaredMethodInfo(parentName, methodName, methodDescriptor);
      DeclaredType parentType =
          DeclaredType.internal(parentStructure.typeInfo(), List.of(parentFoo), null);

      // Child extends Parent: foo() override (직접 선언)
      String childName = "Child";
      ByteCodeClassStructure childStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()), List.of());
      DeclaredMethodInfo childOverrideFoo =
          getDeclaredMethodInfo(childName, methodName, methodDescriptor);
      DeclaredType childType =
          DeclaredType.internal(childStructure.typeInfo(), List.of(childOverrideFoo), null);

      // GrandChild extends Child: foo() 없음
      String grandChildName = "GrandChild";
      ByteCodeClassStructure grandChildStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, grandChildName, childName, null, List.of()),
              List.of());
      DeclaredType grandChildType =
          DeclaredType.internal(grandChildStructure.typeInfo(), List.of(), null);

      declaredTypeRepository.saveAll(List.of(parentType, childType, grandChildType));

      // Type Graph: Parent -> Child, Child -> GrandChild
      childType.update(parentType, List.of());
      declaredTypeRepository.save(childType);

      grandChildType.update(childType, List.of());
      declaredTypeRepository.save(grandChildType);

      // when
      List<DeclaredMethod> declaredMethods =
          classSuperDispatcher.dispatchSupers(List.of(parentType, childType, grandChildType));

      // then
      List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(parentName, methodName, childName, methodName, MethodOrigin.INTERNAL_DECLARED),
              tuple(
                  childName,
                  methodName,
                  grandChildName,
                  methodName,
                  MethodOrigin.INTERNAL_INHERITED_DECLARATION));
    }
  }
}
