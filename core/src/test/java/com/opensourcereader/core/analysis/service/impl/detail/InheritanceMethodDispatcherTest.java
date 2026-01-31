package com.opensourcereader.core.analysis.service.impl.detail;

import static com.opensourcereader.core.analysis.testfixture.CallGraphTestSupport.getOutgoingCallEdges;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.entity.MethodCallEdge;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent.RepoEntryType;
import com.opensourcereader.core.analysis.entity.method.MethodOrigin;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.repository.MethodRepository;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;
import com.opensourcereader.core.analysis.testfixture.TestRepoFixtures;
import com.opensourcereader.core.analysis.testfixture.TestTypeFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class InheritanceMethodDispatcherTest {

  @Autowired private TypeRepository typeRepository;
  @Autowired private MethodRepository methodRepository;
  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;
  @Autowired private InheritanceMethodDispatcher inheritanceMethodDispatcher;

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
          TestTypeFixtures.createTypeWithMethod(
              parentName + "path",
              RepoEntryType.FILE,
              parentName,
              methodName,
              methodDescriptor,
              TypeKind.CLASS);
      TypeStructure childType =
          TestTypeFixtures.createTypeWithoutMethod(
              childName + "path", RepoEntryType.FILE, childName, TypeKind.CLASS);
      OpenSourceRepo repo =
          TestRepoFixtures.saveRepo(
              openSourceRepoRepository, "new-cloneUrl", List.of(childType, parentType));

      TestRepoFixtures.linkInheritance(typeRepository, repo.getId(), childName, parentName);

      // when
      inheritanceMethodDispatcher.connectInheritance(repo.getId());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methodRepository.findAll());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(
                  parentName, methodName, childName, methodName, MethodOrigin.INHERITED_INTERNAL));
    }

    @Test
    @DisplayName("1-2. override가 있으면 Parent.foo -> Child.foo(override) 로 연결된다")
    void givenOverrideExists_whenDispatchSupers_thenLinkParentFooToChildFooOverride() {
      // given
      String parentName = "Parent";
      String childName = "Child";
      String methodName = "foo";
      MethodDescriptor md = MethodDescriptor.from("()V");

      TypeStructure parent =
          TestTypeFixtures.createTypeWithMethod(
              "parent", RepoEntryType.FILE, parentName, methodName, md, TypeKind.CLASS);
      TypeStructure child =
          TestTypeFixtures.createTypeWithMethod(
              "child", RepoEntryType.FILE, childName, methodName, md, TypeKind.CLASS);
      OpenSourceRepo repo =
          TestRepoFixtures.saveRepo(
              openSourceRepoRepository, "new-cloneUrl", List.of(child, parent));

      TestRepoFixtures.linkInheritance(typeRepository, repo.getId(), childName, parentName);

      // when
      inheritanceMethodDispatcher.connectInheritance(repo.getId());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methodRepository.findAll());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(parentName, methodName, childName, methodName, MethodOrigin.DECLARED));
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
          TestTypeFixtures.createTypeWithMethod(
              "parent",
              RepoEntryType.FILE,
              parentName,
              methodName,
              methodDescriptor,
              TypeKind.CLASS);
      TypeStructure child =
          TestTypeFixtures.createTypeWithoutMethod(
              "child", RepoEntryType.FILE, childName, TypeKind.CLASS);
      TypeStructure grandChild =
          TestTypeFixtures.createTypeWithoutMethod(
              "grandChild", RepoEntryType.FILE, grandChildName, TypeKind.CLASS);

      OpenSourceRepo repo =
          TestRepoFixtures.saveRepo(
              openSourceRepoRepository, "new-cloneUrl", List.of(grandChild, child, parent));

      TestRepoFixtures.linkInheritance(typeRepository, repo.getId(), childName, parentName);
      TestRepoFixtures.linkInheritance(typeRepository, repo.getId(), grandChildName, childName);

      // when
      inheritanceMethodDispatcher.connectInheritance(repo.getId());

      // then
      List<MethodCallEdge> outgoingCalls = getOutgoingCallEdges(methodRepository.findAll());
      assertThat(outgoingCalls)
          .extracting(
              e -> e.getCaller().getTypeInternalName(),
              e -> e.getCaller().getMethodName(),
              e -> e.getCallee().getTypeInternalName(),
              e -> e.getCallee().getMethodName(),
              e -> e.getCallee().getOrigin())
          .containsExactlyInAnyOrder(
              tuple(parentName, methodName, childName, methodName, MethodOrigin.INHERITED_INTERNAL),
              tuple(
                  childName,
                  methodName,
                  grandChildName,
                  methodName,
                  MethodOrigin.INHERITED_INTERNAL));
    }
  }
}
