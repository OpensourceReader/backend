package com.opensourcereader.core.analysis.service.impl.callgraph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.method.MethodDescriptor;
import com.opensourcereader.core.analysis.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.entity.method.methodcall.CodeMethodCallEdge;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class PolymorphicEdgeComputerTest {

  @Autowired private PolymorphicEdgeComputer edgeComputer;
  @Autowired private DeclaredTypeRepository declaredTypeRepository;

  @Nested
  @DisplayName("1. 인터페이스 기본")
  class InterfaceBasics {

    @Test
    @DisplayName("1-1. 인터페이스 I의 메서드 -> (직접) 구현체 A의 메서드로 연결된다")
    void interface_to_first_implementor() {
      // given
      String methodName = "method";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");
      String interfaceName = "interface";
      ClassStructure interfaceStructure =
          new ClassStructure(
              new ClassInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
              List.of());
      CodeMethodExtractResult methodExtractResult =
          new CodeMethodExtractResult(
              methodName,
              EnumSet.of(MethodModifier.PUBLIC),
              methodDescriptor.methodReturnType(),
              methodDescriptor.argumentTypes(),
              1,
              1);
      DeclaredType interfaceType =
          DeclaredType.internal(interfaceStructure, List.of(methodExtractResult));

      // 2) 구현체 A (I를 implements 하는 CLASS)
      String className = "A";
      ClassStructure implStructure =
          new ClassStructure(
              new ClassInfo(33, TypeKind.CLASS, className, null, null, List.of("I")), List.of());
      DeclaredType implClassType =
          DeclaredType.internal(implStructure, List.of(methodExtractResult));
      declaredTypeRepository.saveAll(List.of(interfaceType, implClassType));
      implClassType.update(
          null,
          List.of(DeclaredTypeImplementEdge.of(implClassType, interfaceType))); // 구현체 기준으로 들어감
      declaredTypeRepository.save(implClassType);

      // when
      List<CodeMethodCallEdge> result =
          edgeComputer.compute(List.of(interfaceType), List.of(implClassType));

      // then
      assertThat(result).hasSize(1);
      assertThat(result)
          .extracting(
              edge -> edge.getCaller().getTypeInternalName(),
              edge -> edge.getCaller().getMethodName(),
              edge -> edge.getCallee().getTypeInternalName(),
              edge -> edge.getCallee().getMethodName())
          .containsExactlyInAnyOrder(tuple(interfaceName, methodName, className, methodName));
    }

    @Test
    @DisplayName("1-2. 인터페이스는 첫 구현체(A)까지만 연결하고, 하위 클래스(B extends A)는 클래스A -> 클래스B로 연결한다.")
    void interface_only_first_implementor_ignore_subclass() {
      // given
      String methodName = "method";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      // 1) 인터페이스 I
      String interfaceName = "I";
      ClassStructure interfaceStructure =
          new ClassStructure(
              new ClassInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
              List.of());
      CodeMethodExtractResult methodExtractResult =
          new CodeMethodExtractResult(
              methodName,
              EnumSet.of(MethodModifier.PUBLIC),
              methodDescriptor.methodReturnType(),
              methodDescriptor.argumentTypes(),
              1,
              1);
      DeclaredType interfaceType =
          DeclaredType.internal(interfaceStructure, List.of(methodExtractResult));

      // 2) 구현체 A (implements I)
      String classAName = "A";
      ClassStructure implAStructure =
          new ClassStructure(
              new ClassInfo(33, TypeKind.CLASS, classAName, null, null, List.of(interfaceName)),
              List.of());
      DeclaredType implAType = DeclaredType.internal(implAStructure, List.of(methodExtractResult));

      // 3) 하위 클래스 B (extends A)
      String classBName = "B";
      ClassStructure implBStructure =
          new ClassStructure(
              // superName 자리에 "A"를 넣는 형태라면 이렇게
              new ClassInfo(33, TypeKind.CLASS, classBName, classAName, null, List.of()),
              List.of());
      DeclaredType implBType = DeclaredType.internal(implBStructure, List.of(methodExtractResult));
      declaredTypeRepository.saveAll(List.of(interfaceType, implAType, implBType));

      // A 기준으로 implements edge 세팅
      implAType.update(null, List.of(DeclaredTypeImplementEdge.of(implAType, interfaceType)));
      declaredTypeRepository.save(implAType);

      // B의 super A연결
      implBType.update(implAType, List.of());
      declaredTypeRepository.save(implBType);

      // when
      // 그런데 이렇게 들어갔으면... A -> B 연결이 되어야하는데;;, 왜 연결이 안되있지
      // super도 연결을 해줬어야되는데,, 이게 아니여서 그런가?
      List<CodeMethodCallEdge> result =
          edgeComputer.compute(List.of(interfaceType), List.of(implAType, implBType));

      // then
      assertThat(result).hasSize(2);
      assertThat(result)
          .extracting(
              edge -> edge.getCaller().getTypeInternalName(),
              edge -> edge.getCaller().getMethodName(),
              edge -> edge.getCallee().getTypeInternalName(),
              edge -> edge.getCallee().getMethodName())
          .containsExactlyInAnyOrder(
              tuple(interfaceName, methodName, classAName, methodName),
              tuple(classAName, methodName, classBName, methodName));
    }

    // =========================
    // 2) 인터페이스 상속(extends)
    // =========================
    @Nested
    @DisplayName("2. 인터페이스 extends")
    class InterfaceExtends {

      @Test
      @DisplayName(
          "2-1. 인터페이스 I1.foo 명세가 I2로 계승되며, I1.foo -> I2.foo 연결이 생성된다(인터페이스 상속은 implmenentedInterfaces에 등록된다.)")
      void interface_extends_interface_spec_inheritance() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        // I1: foo()
        String i1Name = "I1";
        ClassStructure i1Structure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, i1Name, null, null, List.of()), List.of());
        CodeMethodExtractResult foo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);
        DeclaredType i1Type = DeclaredType.internal(i1Structure, List.of(foo));

        // I2: extends I1, + foo()를 "명시적으로" 선언(테스트 편의상)
        String i2Name = "I2";
        ClassStructure i2Structure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, i2Name, i1Name, null, List.of()), List.of());
        DeclaredType i2Type = DeclaredType.internal(i2Structure, List.of(foo));

        declaredTypeRepository.saveAll(List.of(i1Type, i2Type));

        // (선택) extends edge를 별도 엔티티로 관리한다면 여기서 업데이트/저장
        i2Type.update(null, List.of(DeclaredTypeImplementEdge.of(i2Type, i1Type)));
        declaredTypeRepository.save(i2Type);

        // when
        List<CodeMethodCallEdge> result = edgeComputer.compute(List.of(i1Type, i2Type), List.of());

        // then
        assertThat(result).hasSize(1);
        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName())
            .containsExactlyInAnyOrder(
                tuple(i1Name, methodName, i2Name, methodName)); // 순서가 다르게 나왔는데, I1 -> I2인데??
      }

      @Test
      @DisplayName("2-2. 인터페이스 계층(I1 -> I2) 후, I2의 첫 구현체 A까지 연결된다")
      void interface_chain_then_first_implementor() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        CodeMethodExtractResult foo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        // I1: foo()
        String i1Name = "I1";
        ClassStructure i1Structure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, i1Name, null, null, List.of()), List.of());
        DeclaredType i1Type = DeclaredType.internal(i1Structure, List.of(foo));

        // I2: extends I1 (테스트 단순화를 위해 foo()도 "명시적으로" 넣음)
        String i2Name = "I2";
        ClassStructure i2Structure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, i2Name, i1Name, null, List.of()),
                // superName=I1 가정
                List.of());
        DeclaredType i2Type = DeclaredType.internal(i2Structure, List.of(foo));

        // A: implements I2
        String aName = "A";
        ClassStructure aStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, aName, null, null, List.of(i2Name)), List.of());
        DeclaredType aType = DeclaredType.internal(aStructure, List.of(foo));
        declaredTypeRepository.saveAll(List.of(i1Type, i2Type, aType));

        // I2 extends I1 (프로젝트에 맞게 edge 이름 수정)
        i2Type.update(null, List.of(DeclaredTypeImplementEdge.of(i2Type, i1Type)));
        declaredTypeRepository.save(i2Type);

        // A implements I2
        aType.update(null, List.of(DeclaredTypeImplementEdge.of(aType, i2Type)));
        declaredTypeRepository.save(aType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(List.of(i1Type, i2Type), List.of(aType));

        // then // 같은거 하나가 더들어가서 문제
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName())
            .containsExactlyInAnyOrder(
                tuple(i1Name, methodName, i2Name, methodName),
                tuple(i2Name, methodName, aName, methodName));
      }
    }

    // =========================
    // 3) 인터페이스 default 메서드
    // =========================
    @Nested
    @DisplayName("3. 인터페이스 default 메서드")
    class InterfaceDefaultMethod {

      @Test
      @DisplayName(
          "3-1. default foo()를 구현체가 override하지 않으면, 구현체에 virtual 메서드를 만들어 부모(default)에서 유래됨을 표시해 연결한다")
      void default_method_no_override_virtual() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        // 1) 인터페이스 I: default foo()
        String interfaceName = "I";
        ClassStructure interfaceStructure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
                List.of());

        // default 메서드임을 표현: MethodModifier.DEFAULT가 너희 모델에 있으면 그걸 쓰고,
        // 없다면 (PUBLIC + inferredDefault)처럼 별도 플래그가 있어야 함.
        CodeMethodExtractResult defaultFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC, MethodModifier.INTERFACE_DEFAULT),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType interfaceType = DeclaredType.internal(interfaceStructure, List.of(defaultFoo));

        // 2) 구현체 A: implements I, foo() 선언 없음 (즉, methodExtractResult 비움)
        String className = "A";
        ClassStructure implStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, className, null, null, List.of(interfaceName)),
                List.of());
        DeclaredType implType = DeclaredType.internal(implStructure, List.of()); // foo를 일부러 넣지 않음

        declaredTypeRepository.saveAll(List.of(interfaceType, implType));

        // A implements I
        implType.update(null, List.of(DeclaredTypeImplementEdge.of(implType, interfaceType)));
        declaredTypeRepository.save(implType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(List.of(interfaceType), List.of(implType));

        // then: 엣지 1개 (I.foo -> A.foo(virtual))
        assertThat(result).hasSize(1);
        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName(),
                e -> e.getCallee().getOrigin())
            .containsExactlyInAnyOrder(
                tuple(
                    interfaceName,
                    methodName,
                    className,
                    methodName,
                    MethodOrigin.INTERNAL_INHERITED_DECLARATION) // I.foo -> A.foo (virtual)
                );
      }

      @Test
      @DisplayName("3-2. default foo()를 구현체가 override하면, I.foo -> A.foo(override)로 연결된다")
      void default_method_with_override() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        // 1) 인터페이스 I: default foo()
        String interfaceName = "I";
        ClassStructure interfaceStructure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
                List.of());

        CodeMethodExtractResult defaultFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC, MethodModifier.INTERFACE_DEFAULT),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType interfaceType = DeclaredType.internal(interfaceStructure, List.of(defaultFoo));

        // 2) 구현체 A: implements I, foo() override(직접 선언)
        String className = "A";
        ClassStructure implStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, className, null, null, List.of(interfaceName)),
                List.of());

        // override 메서드: 보통 PUBLIC만 있어도 됨 (DEFAULT 플래그는 붙이면 안 됨)
        CodeMethodExtractResult overrideFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType implType = DeclaredType.internal(implStructure, List.of(overrideFoo));

        declaredTypeRepository.saveAll(List.of(interfaceType, implType));

        // A implements I
        implType.update(null, List.of(DeclaredTypeImplementEdge.of(implType, interfaceType)));
        declaredTypeRepository.save(implType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(List.of(interfaceType), List.of(implType));

        // then
        assertThat(result).hasSize(1);

        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName(),
                e -> e.getCallee().getOrigin())
            .containsExactlyInAnyOrder(
                tuple(
                    interfaceName,
                    methodName,
                    className,
                    methodName,
                    MethodOrigin.INTERNAL_DECLARED));
      }
    }

    // =========================
    // 4) 클래스 상속 기본
    // =========================
    @Nested
    @DisplayName("4. 클래스 상속")
    class ClassInheritance {

      @Test
      @DisplayName("4-1. override가 없으면 Parent.foo -> Child.foo(virtual)로 연결된다")
      void class_inheritance_no_override_virtual_to_parent() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        CodeMethodExtractResult declaredFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        // 1) Parent (CLASS): foo()
        String parentName = "Parent";
        ClassStructure parentStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
        DeclaredType parentType = DeclaredType.internal(parentStructure, List.of(declaredFoo));

        // 2) Child (CLASS, extends Parent): foo() 선언 없음
        String childName = "Child";
        ClassStructure childStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()),
                List.of());
        DeclaredType childType = DeclaredType.internal(childStructure, List.of()); // foo 없음

        declaredTypeRepository.saveAll(List.of(parentType, childType));

        // Child extends Parent (너희 프로젝트 extends-edge로 교체)
        childType.update(parentType, List.of());
        declaredTypeRepository.save(childType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(
                List.of(), List.of(parentType, childType) // 상속이 중복되는 경우 고려필요
                );

        // then
        assertThat(result).hasSize(1);
        assertThat(result)
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
      @DisplayName("4-2. override가 있으면 Parent.foo -> Child.foo(override) 로 연결된다")
      void class_inheritance_with_override_parent_to_child() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        // Parent.foo (declared)
        CodeMethodExtractResult parentFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        // 1) Parent (CLASS): foo()
        String parentName = "Parent";
        ClassStructure parentStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
        DeclaredType parentType = DeclaredType.internal(parentStructure, List.of(parentFoo));

        // 2) Child (CLASS, extends Parent): foo() override (직접 선언)
        String childName = "Child";
        ClassStructure childStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()),
                List.of());

        CodeMethodExtractResult childOverrideFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC), // override도 보통 PUBLIC이면 충분
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType childType =
            DeclaredType.internal(childStructure, List.of(childOverrideFoo)); // ✅ override 선언

        declaredTypeRepository.saveAll(List.of(parentType, childType));

        // Child extends Parent (너희 update 시그니처 기준: parentType을 바로 넣는 형태)
        childType.update(parentType, List.of());
        declaredTypeRepository.save(childType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(List.of(), List.of(parentType, childType));

        // then
        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName(),
                e -> e.getCallee().getOrigin())
            .containsExactlyInAnyOrder(
                tuple(
                    parentName, methodName, childName, methodName, MethodOrigin.INTERNAL_DECLARED));
      }
    }

    // =========================
    // 5) 다단 상속 체인
    // =========================
    @Nested
    @DisplayName("5. 다단 상속")
    class MultiLevelInheritance {

      // 어디서 내려온 메서드인지는 표시를 해둬야함, 그래야 나중에 찾기가 편함
      // CodeEdge가 추가되면, CodeMethod도 추가되어야합니다.
      @Test
      @DisplayName(
          "5-1. Parent -> Child -> GrandChild 모두 override 없어도, Parent는 Child(Virtual)로 연결, Child는 GrandChild(Virtual)로 연결된다.")
      void chain_no_overrides() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        CodeMethodExtractResult parentFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        // Parent: foo()
        String parentName = "Parent";
        ClassStructure parentStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
        DeclaredType parentType = DeclaredType.internal(parentStructure, List.of(parentFoo));

        // Child extends Parent: foo() 없음
        String childName = "Child";
        ClassStructure childStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()),
                List.of());
        DeclaredType childType = DeclaredType.internal(childStructure, List.of());

        // GrandChild extends Child: foo() 없음
        String grandChildName = "GrandChild";
        ClassStructure grandChildStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, grandChildName, childName, null, List.of()),
                List.of());
        DeclaredType grandChildType = DeclaredType.internal(grandChildStructure, List.of());

        declaredTypeRepository.saveAll(List.of(parentType, childType, grandChildType));

        // 타입 그래프 세팅: Child -> Parent, GrandChild -> Child
        childType.update(parentType, List.of());
        declaredTypeRepository.save(childType);

        grandChildType.update(childType, List.of());
        declaredTypeRepository.save(grandChildType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(List.of(), List.of(parentType, childType, grandChildType));

        // then
        assertThat(result)
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
          "5-2. 상속 체인에서 중간 클래스(Child)가 override를 하면, 하위 클래스(GrandChild)는 Parent가 아닌 Child를 기준으로 연결된다")
      void chain_middle_override() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        // Parent: foo() declared
        CodeMethodExtractResult parentFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        String parentName = "Parent";
        ClassStructure parentStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, parentName, null, null, List.of()), List.of());
        DeclaredType parentType = DeclaredType.internal(parentStructure, List.of(parentFoo));

        // Child extends Parent: foo() override (직접 선언)
        String childName = "Child";
        ClassStructure childStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()),
                List.of());

        CodeMethodExtractResult childOverrideFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType childType = DeclaredType.internal(childStructure, List.of(childOverrideFoo));

        // GrandChild extends Child: foo() 없음
        String grandChildName = "GrandChild";
        ClassStructure grandChildStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, grandChildName, childName, null, List.of()),
                List.of());
        DeclaredType grandChildType = DeclaredType.internal(grandChildStructure, List.of());

        declaredTypeRepository.saveAll(List.of(parentType, childType, grandChildType));

        // Type Graph: Parent -> Child, Child -> GrandChild
        childType.update(parentType, List.of());
        declaredTypeRepository.save(childType);

        grandChildType.update(childType, List.of());
        declaredTypeRepository.save(grandChildType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(List.of(), List.of(parentType, childType, grandChildType));

        // then
        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName(),
                e -> e.getCallee().getOrigin())
            .containsExactlyInAnyOrder(
                tuple(
                    parentName, methodName, childName, methodName, MethodOrigin.INTERNAL_DECLARED),
                tuple(
                    childName,
                    methodName,
                    grandChildName,
                    methodName,
                    MethodOrigin.INTERNAL_INHERITED_DECLARATION));
      }
    }

    // =========================
    // 6) 인터페이스 + 클래스 혼합
    // =========================
    @Nested
    @DisplayName("6. 인터페이스 + 클래스 혼합")
    class InterfaceAndClassMix {

      @Test
      @DisplayName("6-1. 인터페이스 구현(I→Parent)과 클래스 상속(Child→Parent) 규칙은 서로 독립적으로 함께 적용된다")
      void interface_then_class_inheritance_applies_separately() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        // I.foo (interface spec)
        String interfaceName = "I";
        ClassStructure interfaceStructure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
                List.of());

        CodeMethodExtractResult interfaceFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType interfaceType =
            DeclaredType.internal(interfaceStructure, List.of(interfaceFoo));

        // Parent.foo (implements I, and declares foo)
        String parentName = "Parent";
        ClassStructure parentStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, parentName, null, null, List.of(interfaceName)),
                List.of());

        CodeMethodExtractResult parentFoo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        DeclaredType parentType = DeclaredType.internal(parentStructure, List.of(parentFoo));

        // Child extends Parent (foo 없음)
        String childName = "Child";
        ClassStructure childStructure =
            new ClassStructure(
                new ClassInfo(33, TypeKind.CLASS, childName, parentName, null, List.of()),
                List.of());

        DeclaredType childType = DeclaredType.internal(childStructure, List.of()); // foo 없음

        declaredTypeRepository.saveAll(List.of(interfaceType, parentType, childType));

        // Parent implements I
        parentType.update(null, List.of(DeclaredTypeImplementEdge.of(parentType, interfaceType)));
        declaredTypeRepository.save(parentType);

        // Child extends Parent (너희 update 시그니처 기준)
        childType.update(parentType, List.of());
        declaredTypeRepository.save(childType);

        // when
        List<CodeMethodCallEdge> result =
            edgeComputer.compute(
                List.of(interfaceType),
                List.of(parentType, childType) // 구현에 따라 Parent만 넣어도 되면 Parent만 넣어도 됨
                );

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName(),
                e -> e.getCallee().getOrigin())
            .containsExactlyInAnyOrder(
                tuple(
                    interfaceName,
                    methodName,
                    parentName,
                    methodName,
                    MethodOrigin.INTERNAL_DECLARED),
                tuple(
                    parentName,
                    methodName,
                    childName,
                    methodName,
                    MethodOrigin.INTERNAL_INHERITED_DECLARATION));
      }
    }

    // =========================
    // 7) 안전성 / 종료 조건
    // =========================
    @Nested
    @DisplayName("7. 안전성(순환)")
    class Safety {

      @Test
      @DisplayName("7-1. 순환 인터페이스(I1↔I2)는 무한 루프 위험이 있으나, 자바 규칙상 발생하지 않으므로 별도 방어 없이 둔다")
      void cyclic_interface_should_not_infinite_loop() {
        // given
        String methodName = "foo";
        MethodDescriptor md = MethodDescriptor.from("()V");

        CodeMethodExtractResult foo =
            new CodeMethodExtractResult(
                methodName,
                EnumSet.of(MethodModifier.PUBLIC),
                md.methodReturnType(),
                md.argumentTypes(),
                1,
                1);

        // I1
        String i1Name = "I1";
        ClassStructure i1Structure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, i1Name, null, null, List.of()), List.of());
        DeclaredType i1Type = DeclaredType.internal(i1Structure, List.of(foo));

        // I2
        String i2Name = "I2";
        ClassStructure i2Structure =
            new ClassStructure(
                new ClassInfo(183, TypeKind.INTERFACE, i2Name, null, null, List.of()), List.of());
        DeclaredType i2Type = DeclaredType.internal(i2Structure, List.of(foo));

        declaredTypeRepository.saveAll(List.of(i1Type, i2Type));

        // 순환 extends 구성: I1 extends I2, I2 extends I1
        i1Type.update(null, List.of(DeclaredTypeImplementEdge.of(i1Type, i2Type)));
        i2Type.update(null, List.of(DeclaredTypeImplementEdge.of(i2Type, i1Type)));
        declaredTypeRepository.saveAll(List.of(i1Type, i2Type));

        // when + then
        // ✅ 무한 루프면 여기서 타임아웃으로 실패함
        assertThrows(
            org.opentest4j.AssertionFailedError.class,
            () -> {
              assertTimeoutPreemptively(
                  Duration.ofMillis(300),
                  () -> {
                    edgeComputer.compute(List.of(i1Type, i2Type), List.of());
                  });
            });
      }
    }
  }
}
