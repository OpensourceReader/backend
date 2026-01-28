package com.opensourcereader.core.analysis.service.impl.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getDeclaredMethodInfo;
import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
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
import com.opensourcereader.core.analysis.entity.type.DeclaredTypeImplementEdge;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class InterfacePolymorphicDispatcherTest {

  @Autowired private DeclaredTypeRepository declaredTypeRepository;
  @Autowired private InterfacePolymorphicDispatcher dispatcher;

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
      ByteCodeClassStructure interfaceStructure =
          new ByteCodeClassStructure(
              new TypeInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
              List.of());
      DeclaredMethodInfo declaredMethodInfo =
          getDeclaredMethodInfo(interfaceName, methodName, methodDescriptor);
      DeclaredType interfaceType =
          DeclaredType.internal(interfaceStructure.typeInfo(), List.of(declaredMethodInfo), null);

      // 2) 구현체 A (I를 implements 하는 CLASS)
      String className = "A";
      ByteCodeClassStructure implStructure =
          new ByteCodeClassStructure(
              new TypeInfo(33, TypeKind.CLASS, className, null, null, List.of("I")), List.of());
      DeclaredType implClassType =
          DeclaredType.internal(implStructure.typeInfo(), List.of(declaredMethodInfo), null);
      declaredTypeRepository.saveAll(List.of(interfaceType, implClassType));
      implClassType.update(
          null, List.of(DeclaredTypeImplementEdge.of(implClassType, interfaceType)));
      declaredTypeRepository.save(implClassType);

      // when
      List<DeclaredMethod> declaredMethods =
          dispatcher.dispatchImplementations(List.of(interfaceType));

      // then
      List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
      assertThat(outgoingCalls)
          .extracting(
              edge -> edge.getCaller().getTypeInternalName(),
              edge -> edge.getCaller().getMethodName(),
              edge -> edge.getCallee().getTypeInternalName(),
              edge -> edge.getCallee().getMethodName())
          .containsExactlyInAnyOrder(tuple(interfaceName, methodName, className, methodName));
    }

    @Nested
    @DisplayName("2. 인터페이스 extends")
    class InterfaceExtends {

      @Test
      @DisplayName(
          "2-1. 인터페이스 I1.foo 명세가 I2로 계승되며, I1.foo -> I2.foo 연결이 생성된다(인터페이스 상속은 implmenentedInterfaces에 등록된다.)")
      void interface_extends_interface_spec_inheritance() {
        // given
        String methodName = "foo";
        MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

        // I1: foo()
        String i1Name = "I1";
        ByteCodeClassStructure i1Structure =
            new ByteCodeClassStructure(
                new TypeInfo(183, TypeKind.INTERFACE, i1Name, null, null, List.of()), List.of());
        DeclaredMethodInfo foo = getDeclaredMethodInfo(i1Name, methodName, methodDescriptor);
        DeclaredType i1Type = DeclaredType.internal(i1Structure.typeInfo(), List.of(foo), null);

        // I2: extends I1, + foo()를 "명시적으로" 선언(테스트 편의상)
        String i2Name = "I2";
        ByteCodeClassStructure i2Structure =
            new ByteCodeClassStructure(
                new TypeInfo(183, TypeKind.INTERFACE, i2Name, i1Name, null, List.of()), List.of());
        DeclaredType i2Type = DeclaredType.internal(i2Structure.typeInfo(), List.of(foo), null);

        declaredTypeRepository.saveAll(List.of(i1Type, i2Type));

        // (선택) extends edge를 별도 엔티티로 관리한다면 여기서 업데이트/저장
        i2Type.update(null, List.of(DeclaredTypeImplementEdge.of(i2Type, i1Type)));
        declaredTypeRepository.save(i2Type);

        // when
        List<DeclaredMethod> declaredMethods =
            dispatcher.dispatchImplementations(List.of(i1Type, i2Type));

        // then
        List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
        assertThat(outgoingCalls)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName())
            .containsExactlyInAnyOrder(tuple(i1Name, methodName, i2Name, methodName));
      }

      @Test
      @DisplayName("2-2. 인터페이스 계층(I1 -> I2) 후, I2의 첫 구현체 A까지 연결된다")
      void interface_chain_then_first_implementor() {
        // given
        String methodName = "foo";
        MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

        // I1: foo()
        String i1Name = "I1";
        ByteCodeClassStructure i1Structure =
            new ByteCodeClassStructure(
                new TypeInfo(183, TypeKind.INTERFACE, i1Name, null, null, List.of()), List.of());
        DeclaredMethodInfo foo = getDeclaredMethodInfo(i1Name, methodName, methodDescriptor);
        DeclaredType i1Type = DeclaredType.internal(i1Structure.typeInfo(), List.of(foo), null);

        // I2: extends I1 (테스트 단순화를 위해 foo()도 "명시적으로" 넣음)
        String i2Name = "I2";
        ByteCodeClassStructure i2Structure =
            new ByteCodeClassStructure(
                new TypeInfo(183, TypeKind.INTERFACE, i2Name, i1Name, null, List.of()),
                // superName=I1 가정
                List.of());
        DeclaredType i2Type = DeclaredType.internal(i2Structure.typeInfo(), List.of(foo), null);

        // A: implements I2
        String aName = "A";
        ByteCodeClassStructure aStructure =
            new ByteCodeClassStructure(
                new TypeInfo(33, TypeKind.CLASS, aName, null, null, List.of(i2Name)), List.of());
        DeclaredType aType = DeclaredType.internal(aStructure.typeInfo(), List.of(foo), null);
        declaredTypeRepository.saveAll(List.of(i1Type, i2Type, aType));

        // I2 extends I1 (프로젝트에 맞게 edge 이름 수정)
        i2Type.update(null, List.of(DeclaredTypeImplementEdge.of(i2Type, i1Type)));
        declaredTypeRepository.save(i2Type);

        // A implements I2
        aType.update(null, List.of(DeclaredTypeImplementEdge.of(aType, i2Type)));
        declaredTypeRepository.save(aType);

        // when
        List<DeclaredMethod> declaredMethods =
            dispatcher.dispatchImplementations(List.of(i1Type, i2Type));

        // then
        List<CodeMethodCallEdge> outgoingCalls = getOutgoingCallEdges(declaredMethods);
        assertThat(outgoingCalls)
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

    @Nested
    @DisplayName("3. 인터페이스 default 메서드")
    class InterfaceDefaultMethod {

      @Test
      @DisplayName(
          "3-1. default foo()를 구현체가 override하지 않으면, 구현체에 virtual 메서드를 만들어 부모(default)에서 유래됨을 표시해 연결한다")
      void default_method_no_override_virtual() {
        // given
        String methodName = "foo";
        MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

        // 1) 인터페이스 I: default foo()
        String interfaceName = "I";
        ByteCodeClassStructure interfaceStructure =
            new ByteCodeClassStructure(
                new TypeInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
                List.of());

        // default 메서드임을 표현: MethodModifier.DEFAULT가 너희 모델에 있으면 그걸 쓰고,
        // 없다면 (PUBLIC + inferredDefault)처럼 별도 플래그가 있어야 함.
        DeclaredMethodInfo defaultFoo =
            getDeclaredMethodInfo(interfaceName, methodName, methodDescriptor);
        DeclaredType interfaceType =
            DeclaredType.internal(interfaceStructure.typeInfo(), List.of(defaultFoo), null);

        // 2) 구현체 A: implements I, foo() 선언 없음 (즉, methodExtractResult 비움)
        String className = "A";
        ByteCodeClassStructure implStructure =
            new ByteCodeClassStructure(
                new TypeInfo(33, TypeKind.CLASS, className, null, null, List.of(interfaceName)),
                List.of());
        DeclaredType implType =
            DeclaredType.internal(implStructure.typeInfo(), List.of(), null); // foo를 일부러 넣지 않음

        declaredTypeRepository.saveAll(List.of(interfaceType, implType));

        // A implements I
        implType.update(null, List.of(DeclaredTypeImplementEdge.of(implType, interfaceType)));
        declaredTypeRepository.save(implType);

        // when
        List<DeclaredMethod> declaredMethods =
            dispatcher.dispatchImplementations(List.of(interfaceType));

        // then: 엣지 1개 (I.foo -> A.foo(virtual))
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
        MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

        // 1) 인터페이스 I: default foo()
        String interfaceName = "I";
        ByteCodeClassStructure interfaceStructure =
            new ByteCodeClassStructure(
                new TypeInfo(183, TypeKind.INTERFACE, interfaceName, null, null, List.of()),
                List.of());

        DeclaredMethodInfo defaultFoo =
            getDeclaredMethodInfo(interfaceName, methodName, methodDescriptor);
        DeclaredType interfaceType =
            DeclaredType.internal(interfaceStructure.typeInfo(), List.of(defaultFoo), null);

        // 2) 구현체 A: implements I, foo() override(직접 선언)
        String className = "A";
        ByteCodeClassStructure implStructure =
            new ByteCodeClassStructure(
                new TypeInfo(33, TypeKind.CLASS, className, null, null, List.of(interfaceName)),
                List.of());

        // override 메서드: 보통 PUBLIC만 있어도 됨 (DEFAULT 플래그는 붙이면 안 됨)
        DeclaredMethodInfo overrideFoo =
            getDeclaredMethodInfo(className, methodName, methodDescriptor);
        DeclaredType implType =
            DeclaredType.internal(implStructure.typeInfo(), List.of(overrideFoo), null);

        declaredTypeRepository.saveAll(List.of(interfaceType, implType));

        // A implements I
        implType.update(null, List.of(DeclaredTypeImplementEdge.of(implType, interfaceType)));
        declaredTypeRepository.save(implType);

        // when
        List<DeclaredMethod> declaredMethods =
            dispatcher.dispatchImplementations(List.of(interfaceType));

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
                    interfaceName,
                    methodName,
                    className,
                    methodName,
                    MethodOrigin.INTERNAL_DECLARED));
      }
    }
  }

  @Nested
  @DisplayName("7. 안전성(순환)")
  class Safety {

    @Test
    @DisplayName("7-1. 순환 인터페이스(I1↔I2)는 무한 루프 위험이 있으나, 자바 규칙상 발생하지 않으므로 별도 방어 없이 둔다")
    void cyclic_interface_should_not_infinite_loop() {
      // given
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      // I1
      String i1Name = "I1";
      ByteCodeClassStructure i1Structure =
          new ByteCodeClassStructure(
              new TypeInfo(183, TypeKind.INTERFACE, i1Name, null, null, List.of()), List.of());
      DeclaredMethodInfo foo = getDeclaredMethodInfo(i1Name, methodName, methodDescriptor);
      DeclaredType i1Type = DeclaredType.internal(i1Structure.typeInfo(), List.of(foo), null);

      // I2
      String i2Name = "I2";
      ByteCodeClassStructure i2Structure =
          new ByteCodeClassStructure(
              new TypeInfo(183, TypeKind.INTERFACE, i2Name, null, null, List.of()), List.of());
      DeclaredMethodInfo foo2 = getDeclaredMethodInfo(i2Name, methodName, methodDescriptor);
      DeclaredType i2Type = DeclaredType.internal(i2Structure.typeInfo(), List.of(foo2), null);

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
                  dispatcher.dispatchImplementations(List.of(i1Type, i2Type));
                });
          });
    }
  }
}
