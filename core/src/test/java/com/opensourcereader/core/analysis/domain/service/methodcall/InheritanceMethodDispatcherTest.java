package com.opensourcereader.core.analysis.domain.service.methodcall;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeStructureWithMethod;
import static com.opensourcereader.core.analysis.testfixture.TestTypeFixtures.createTypeStructureWithoutMethod;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import com.opensourcereader.core.analysis.domain.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.factory.ExternalTypeStructureFactory;
import com.opensourcereader.core.analysis.domain.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.domain.service.hierarchy.InheritanceLinkService;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.testfixture.TestTypeFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InheritanceMethodDispatcherTest {

  OpenSourceRepoFactory openSourceRepoFactory =
      new OpenSourceRepoFactory(new InheritanceLinkService(), new ExternalTypeStructureFactory());
  InheritanceMethodDispatcher inheritanceMethodDispatcher = new InheritanceMethodDispatcher();

  @Nested
  @DisplayName("1. 클래스 상속")
  class ClassInheritance {

    @Test
    @DisplayName("1-1. override가 없으면 Parent.foo -> Child.foo(virtual)로 연결된다")
    void givenNoOverride_whenDispatchSupers_thenLinkParentFooToChildFooVirtual() {
      // given
      String parentName = "Parent";
      String childName = "Child";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure parentType =
          createTypeStructureWithMethod(
              parentName, null, null, methodName, methodDescriptor, TypeKind.CLASS);
      TypeStructure childType =
          createTypeStructureWithoutMethod(childName, TypeKind.CLASS, parentName, null);
      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(childType, parentType));

      // when
      inheritanceMethodDispatcher.connectInheritance(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getType().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getType().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(
                  parentName,
                  methodName,
                  childName,
                  methodName,
                  MethodOrigin.VIRTUAL_INTERNAL_INHERITED));
    }

    @Test
    @DisplayName("1-2. override가 있으면 Parent.foo -> Child.foo(override) 로 연결된다")
    void givenOverrideExists_whenDispatchSupers_thenLinkParentFooToChildFooOverride() {
      // given
      String parentName = "Parent";
      String childName = "Child";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure parent =
          createTypeStructureWithMethod(
              parentName, null, null, methodName, methodDescriptor, TypeKind.CLASS);
      TypeStructure child =
          createTypeStructureWithMethod(
              childName, parentName, null, methodName, methodDescriptor, TypeKind.CLASS);
      OpenSourceRepo repo = openSourceRepoFactory.create("new-cloneUrl", List.of(child, parent));

      // when
      inheritanceMethodDispatcher.connectInheritance(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getType().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getType().getTypeInternalName(),
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
      String parentName = "Parent";
      String childName = "Child";
      String grandChildName = "GrandChild";
      String methodName = "foo";
      MethodDescriptor methodDescriptor = MethodDescriptor.from("()V");

      TypeStructure parent =
          createTypeStructureWithMethod(
              parentName, null, null, methodName, methodDescriptor, TypeKind.CLASS);
      TypeStructure child =
          TestTypeFixtures.createTypeStructureWithoutMethod(
              childName, TypeKind.CLASS, parentName, null);
      TypeStructure grandChild =
          TestTypeFixtures.createTypeStructureWithoutMethod(
              grandChildName, TypeKind.CLASS, childName, null);

      OpenSourceRepo repo =
          openSourceRepoFactory.create("new-cloneUrl", List.of(grandChild, child, parent));

      // when
      inheritanceMethodDispatcher.connectInheritance(repo.getTypes());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(repo.getTypes());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getType().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getType().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(
                  parentName,
                  methodName,
                  childName,
                  methodName,
                  MethodOrigin.VIRTUAL_INTERNAL_INHERITED),
              tuple(
                  childName,
                  methodName,
                  grandChildName,
                  methodName,
                  MethodOrigin.VIRTUAL_INTERNAL_INHERITED));
    }
  }
}
