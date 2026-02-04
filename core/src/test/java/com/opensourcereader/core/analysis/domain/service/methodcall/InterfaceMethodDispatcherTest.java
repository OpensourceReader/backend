package com.opensourcereader.core.analysis.domain.service.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeStructureWithMethod;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeStructureWithoutMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepoFactory;
import com.opensourcereader.core.analysis.domain.entity.factory.ExternalTypeStructureFactory;
import com.opensourcereader.core.analysis.domain.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.domain.service.hierarchy.InheritanceLinker;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InterfaceMethodDispatcherTest {

  OpenSourceRepoFactory openSourceRepoFactory =
      new OpenSourceRepoFactory(new InheritanceLinker(), new ExternalTypeStructureFactory());
  InterfaceMethodDispatcher dispatcher = new InterfaceMethodDispatcher(new TypeGraphValidator());

  @Nested
  @DisplayName("1. 인터페이스 기본")
  class InterfaceBasics {

    @Test
    @DisplayName("1-1. 인터페이스 I의 메서드 -> (직접) 구현체 A의 메서드로 연결된다")
    void interface_to_first_implementor() {
      // given
      String interfaceName = "interface";
      String implName = "A";
      String methodName = "method";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure interfaceType =
          createTypeStructureWithMethod(
              interfaceName, null, null, methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure implClassType =
          createTypeStructureWithMethod(
              implName,
              null,
              List.of(interfaceName),
              methodName,
              methodDescriptor,
              TypeKind.INTERFACE);
      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(implClassType, interfaceType));

      // when
      dispatcher.connectInterfaceImplementations(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
      assertThat(outgoingCalls)
          .extracting(
              edge -> edge.getCaller().getTypeInternalName(),
              edge -> edge.getCaller().getMethodName(),
              edge -> edge.getCallee().getTypeInternalName(),
              edge -> edge.getCallee().getMethodName())
          .containsExactlyInAnyOrder(tuple(interfaceName, methodName, implName, methodName));
    }
  }

  @Nested
  @DisplayName("2. 인터페이스 extends")
  class InterfaceExtends {

    @Test
    @DisplayName(
        "2-1. 인터페이스 I1.foo 명세가 I2로 계승되며, I1.foo -> I2.foo 연결이 생성된다(인터페이스 상속은 implmenentedInterfaces에 등록된다.)")
    void interface_extends_interface_spec_inheritance() {
      // given
      String i1Name = "I1";
      String i2Name = "I2";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure interface1Type =
          createTypeStructureWithMethod(
              i1Name, null, null, methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure interface2Type =
          createTypeStructureWithoutMethod(i2Name, TypeKind.INTERFACE, null, List.of(i1Name));
      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(interface1Type, interface2Type));

      // when
      dispatcher.connectInterfaceImplementations(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
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
      String i1Name = "I1";
      String i2Name = "I2";
      String implAName = "A";
      TypeStructure interface1Type =
          createTypeStructureWithMethod(
              i1Name, null, null, methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure interface2Type =
          createTypeStructureWithMethod(
              i2Name, null, List.of(i1Name), methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure implAType =
          createTypeStructureWithMethod(
              implAName, null, List.of(i2Name), methodName, methodDescriptor, TypeKind.CLASS);
      OpenSourceRepo repo =
          openSourceRepoFactory.create(
              "new-cloneUrl", List.of(implAType, interface2Type, interface1Type));

      // when
      dispatcher.connectInterfaceImplementations(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName())
          .containsExactlyInAnyOrder(
              tuple(i1Name, methodName, i2Name, methodName),
              tuple(i2Name, methodName, implAName, methodName));
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
      String interfaceName = "I";
      String implName = "A";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");
      TypeStructure interfaceType =
          createTypeStructureWithMethod(
              interfaceName, null, null, methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure implType =
          createTypeStructureWithoutMethod(implName, TypeKind.CLASS, null, List.of(interfaceName));
      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(implType, interfaceType));

      // when
      dispatcher.connectInterfaceImplementations(repo.getTypes());

      // then: 엣지 1개 (I.foo -> A.foo(virtual))
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
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
                  implName,
                  methodName,
                  MethodOrigin.INTERNAL_INHERITED_VIRTUAL) // I.foo -> A.foo (virtual)
              );
    }

    @Test
    @DisplayName("3-2. default foo()를 구현체가 override하면, I.foo -> A.foo(override)로 연결된다")
    void default_method_with_override() {
      // given
      String interfaceName = "I";
      String implName = "A";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure interfaceType =
          createTypeStructureWithMethod(
              interfaceName, null, null, methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure implType =
          createTypeStructureWithMethod(
              implName, null, List.of(interfaceName), methodName, methodDescriptor, TypeKind.CLASS);
      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(interfaceType, implType));

      // when
      dispatcher.connectInterfaceImplementations(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(
                  interfaceName, methodName, implName, methodName, MethodOrigin.INTERNAL_DECLARED));
    }
  }

  @Nested
  @DisplayName("4. 안전성(순환)")
  class Safety {

    @Test
    @DisplayName(
        "4-1. 순환 인터페이스(I1↔I2)는 자바 규칙상 해당케이스는 발생하지 않으나, 코드상 무한 루프의 위험이 있으므로, 유효성 검사를 통해 예외처리한다")
    void cyclic_interface_should_not_infinite_loop() {
      // given
      String i1Name = "I1";
      String i2Name = "I2";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure interface1Type =
          createTypeStructureWithMethod(
              i1Name, null, List.of(i2Name), methodName, methodDescriptor, TypeKind.INTERFACE);
      TypeStructure interface2Type =
          createTypeStructureWithMethod(
              i2Name, null, List.of(i1Name), methodName, methodDescriptor, TypeKind.INTERFACE);
      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(interface2Type, interface1Type));

      // when + then
      assertThatThrownBy(() -> dispatcher.connectInterfaceImplementations(repo.getTypes()))
          .isInstanceOf(IllegalStateException.class);
    }
  }
}
