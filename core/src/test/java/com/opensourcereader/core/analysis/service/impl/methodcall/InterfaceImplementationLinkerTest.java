package com.opensourcereader.core.analysis.service.impl.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.method.Method;
import com.opensourcereader.core.analysis.entity.method.MethodCallEdge;
import com.opensourcereader.core.analysis.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.RepoEntryType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.testfixture.TestRepoFixtures;
import com.opensourcereader.core.analysis.testfixture.TestTypeFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class InterfaceImplementationLinkerTest {

  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;
  @Autowired private DeclaredTypeRepository declaredTypeRepository;
  @Autowired private InterfaceImplementationLinker dispatcher;

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
          TestTypeFixtures.createTypeWithMethod(
              "interface",
              RepoEntryType.FILE,
              interfaceName,
              methodName,
              methodDescriptor,
              TypeKind.INTERFACE);
      TypeStructure implClassType =
          TestTypeFixtures.createTypeWithMethod(
              "impl",
              RepoEntryType.FILE,
              implName,
              methodName,
              methodDescriptor,
              TypeKind.INTERFACE);
      OpenSourceRepo repo =
          TestRepoFixtures.saveRepo(
              openSourceRepoRepository, "new-cloneUrl", List.of(implClassType, interfaceType));

      DeclaredType interType =
          declaredTypeRepository
              .findByRepoAndTypeInternalName(repo.getId(), interfaceName)
              .orElseThrow();
      DeclaredType implType =
          declaredTypeRepository
              .findByRepoAndTypeInternalName(repo.getId(), implName)
              .orElseThrow();
      implType.updateRelations(null, List.of(interType));
      declaredTypeRepository.save(implType);

      // when
      List<Method> methods = dispatcher.linkAllInterfaceImplementations(repo.getId());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methods);
      assertThat(outgoingCalls)
          .extracting(
              edge -> edge.getCaller().getTypeInternalName(),
              edge -> edge.getCaller().getMethodName(),
              edge -> edge.getCallee().getTypeInternalName(),
              edge -> edge.getCallee().getMethodName())
          .containsExactlyInAnyOrder(tuple(interfaceName, methodName, implName, methodName));
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
            TestTypeFixtures.createTypeWithMethod(
                "inter1",
                RepoEntryType.FILE,
                i1Name,
                methodName,
                methodDescriptor,
                TypeKind.INTERFACE);
        TypeStructure interface2Type =
            TestTypeFixtures.createTypeWithoutMethod(
                "inter2", RepoEntryType.FILE, i2Name, TypeKind.INTERFACE);
        OpenSourceRepo repo =
            TestRepoFixtures.saveRepo(
                openSourceRepoRepository, "new-cloneUrl", List.of(interface2Type, interface1Type));

        DeclaredType inter1Type =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), i1Name)
                .orElseThrow();
        DeclaredType inter2Type =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), i2Name)
                .orElseThrow();
        inter2Type.updateRelations(null, List.of(inter1Type));
        declaredTypeRepository.save(inter2Type);

        // when
        List<Method> methods = dispatcher.linkAllInterfaceImplementations(repo.getId());

        // then
        List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methods);
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
            TestTypeFixtures.createTypeWithMethod(
                "interface",
                RepoEntryType.FILE,
                i1Name,
                methodName,
                methodDescriptor,
                TypeKind.INTERFACE);
        TypeStructure interface2Type =
            TestTypeFixtures.createTypeWithMethod(
                "impl",
                RepoEntryType.FILE,
                i2Name,
                methodName,
                methodDescriptor,
                TypeKind.INTERFACE);
        TypeStructure implAType =
            TestTypeFixtures.createTypeWithMethod(
                "impl",
                RepoEntryType.FILE,
                implAName,
                methodName,
                methodDescriptor,
                TypeKind.CLASS);
        OpenSourceRepo repo =
            TestRepoFixtures.saveRepo(
                openSourceRepoRepository,
                "new-cloneUrl",
                List.of(implAType, interface2Type, interface1Type));

        DeclaredType inter1Type =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), i1Name)
                .orElseThrow();
        DeclaredType inter2Type =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), i2Name)
                .orElseThrow();
        DeclaredType impAType =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), implAName)
                .orElseThrow();
        inter2Type.updateRelations(null, List.of(inter1Type));
        impAType.updateRelations(null, List.of(inter2Type));
        declaredTypeRepository.saveAll(List.of(inter2Type, impAType));

        // when
        List<Method> methods = dispatcher.linkAllInterfaceImplementations(repo.getId());

        // then
        List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methods);
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
            TestTypeFixtures.createTypeWithMethod(
                "impl",
                RepoEntryType.FILE,
                interfaceName,
                methodName,
                methodDescriptor,
                TypeKind.INTERFACE);
        TypeStructure implType =
            TestTypeFixtures.createTypeWithoutMethod(
                "impl", RepoEntryType.FILE, implName, TypeKind.CLASS);
        OpenSourceRepo repo =
            TestRepoFixtures.saveRepo(
                openSourceRepoRepository, "new-cloneUrl", List.of(implType, interfaceType));

        DeclaredType interface1Type =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), interfaceName)
                .orElseThrow();
        DeclaredType implAType =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), implName)
                .orElseThrow();
        implAType.updateRelations(null, List.of(interface1Type));
        declaredTypeRepository.save(implAType);

        // when
        List<Method> methods = dispatcher.linkAllInterfaceImplementations(repo.getId());

        // then: 엣지 1개 (I.foo -> A.foo(virtual))
        List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methods);
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
                    MethodOrigin.INHERITED_INTERNAL) // I.foo -> A.foo (virtual)
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
            TestTypeFixtures.createTypeWithMethod(
                "inter",
                RepoEntryType.FILE,
                interfaceName,
                methodName,
                methodDescriptor,
                TypeKind.INTERFACE);
        TypeStructure implType =
            TestTypeFixtures.createTypeWithMethod(
                "impl", RepoEntryType.FILE, implName, methodName, methodDescriptor, TypeKind.CLASS);
        OpenSourceRepo repo =
            TestRepoFixtures.saveRepo(
                openSourceRepoRepository, "new-cloneUrl", List.of(interfaceType, implType));

        DeclaredType interface1Type =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), interfaceName)
                .orElseThrow();
        DeclaredType implAType =
            declaredTypeRepository
                .findByRepoAndTypeInternalName(repo.getId(), implName)
                .orElseThrow();
        implAType.updateRelations(null, List.of(interface1Type));
        declaredTypeRepository.save(implAType);

        // A implements I
        implAType.updateRelations(null, List.of(interface1Type));
        declaredTypeRepository.save(implAType);

        // when
        List<Method> methods = dispatcher.linkAllInterfaceImplementations(repo.getId());

        // then
        List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methods);
        assertThat(outgoingCalls)
            .extracting(
                e -> e.getCaller().getTypeInternalName(),
                e -> e.getCaller().getMethodName(),
                e -> e.getCallee().getTypeInternalName(),
                e -> e.getCallee().getMethodName(),
                e -> e.getCallee().getOrigin())
            .containsExactlyInAnyOrder(
                tuple(interfaceName, methodName, implName, methodName, MethodOrigin.DECLARED));
      }
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
          TestTypeFixtures.createTypeWithMethod(
              "inter1",
              RepoEntryType.FILE,
              i1Name,
              methodName,
              methodDescriptor,
              TypeKind.INTERFACE);
      TypeStructure interface2Type =
          TestTypeFixtures.createTypeWithMethod(
              "inter2",
              RepoEntryType.FILE,
              i2Name,
              methodName,
              methodDescriptor,
              TypeKind.INTERFACE);
      OpenSourceRepo repo =
          TestRepoFixtures.saveRepo(
              openSourceRepoRepository, "new-cloneUrl", List.of(interface2Type, interface1Type));

      DeclaredType i1Type =
          declaredTypeRepository.findByRepoAndTypeInternalName(repo.getId(), i1Name).orElseThrow();
      DeclaredType i2Type =
          declaredTypeRepository.findByRepoAndTypeInternalName(repo.getId(), i2Name).orElseThrow();

      // 순환 extends 구성: I1 extends I2, I2 extends I1
      i1Type.updateRelations(null, List.of(i2Type));
      i2Type.updateRelations(null, List.of(i1Type));
      declaredTypeRepository.saveAll(List.of(i1Type, i2Type));

      // when + then
      assertThatThrownBy(() -> dispatcher.linkAllInterfaceImplementations(repo.getId()))
          .isInstanceOf(IllegalStateException.class);
    }
  }
}
