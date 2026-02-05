package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.domain.entity.Method;
import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;
import com.opensourcereader.core.analysis.domain.entity.method.MethodModifier;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.domain.entity.type.TypeOrigin;
import com.opensourcereader.core.analysis.dto.MethodCallInfo;
import com.opensourcereader.core.analysis.dto.MethodDescriptor;
import com.opensourcereader.core.analysis.dto.MethodInfo;
import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.service.OpenSourceRepoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class OpenSourceRepoServiceImplTest {

  @Autowired OpenSourceRepoService openSourceRepoService;

  private static final int INVOKE_INTERFACE = 185;

  @Test
  @DisplayName("internalDeclared된 TypeMethod는 TypeStructure를 기반으로 파일과 타입을 생성한다")
  void create_should_create_files_and_types() {
    // given
    String typeInternalName = "com/example/A";
    TypeInfo typeInfo = new TypeInfo(9, TypeKind.CLASS, typeInternalName, null, null, List.of());

    String methodInternalName = "foo";
    MethodInfo methodInfo =
        new MethodInfo(
            typeInternalName,
            methodInternalName,
            EnumSet.noneOf(MethodModifier.class),
            MethodDescriptor.from("()V"),
            null,
            null,
            null,
            null);

    MethodStructure methodStructure = new MethodStructure(methodInfo, List.of());

    TypeStructure typeStructure =
        new TypeStructure("src/A.java", RepoFileType.FILE, "", typeInfo, List.of(methodStructure));

    // when
    OpenSourceRepo repo =
        openSourceRepoService.createRepo("https://github.com/test/repo", List.of(typeStructure));

    // then
    assertThat(repo.getFiles()).hasSize(1);
    assertThat(repo.getTypes()).hasSize(1);
    assertThat(repo.getTypes())
        .extracting(Type::getTypeInternalName, type -> type.getMethods().get(0).getMethodName())
        .containsExactlyInAnyOrder(tuple(typeInternalName, methodInternalName));
  }

  @Test
  @DisplayName("internalDeclared는 내부 타입 생성 시 메서드 호출 대상 외부 타입도 함께 생성한다")
  void internalDeclared_should_create_external_types_from_method_calls() {
    // given
    String internalTypeName = "com/example/A";
    String externalTypeName = "java/util/List";
    TypeInfo typeInfo = new TypeInfo(9, TypeKind.CLASS, internalTypeName, null, null, List.of());
    String internalMethodName = "foo";
    MethodInfo methodInfo =
        new MethodInfo(
            internalTypeName,
            internalMethodName,
            EnumSet.noneOf(MethodModifier.class),
            MethodDescriptor.from("()V"),
            null,
            null,
            null,
            null);
    String externalMethodName = "size";
    MethodCallInfo calleeInfo =
        new MethodCallInfo(
            9, externalTypeName, externalMethodName, MethodDescriptor.from("()I"), false);
    MethodStructure methodStructure = new MethodStructure(methodInfo, List.of(calleeInfo));

    TypeStructure typeStructure =
        new TypeStructure("A.java", RepoFileType.FILE, "", typeInfo, List.of(methodStructure));

    // when
    OpenSourceRepo repo = openSourceRepoService.createRepo("url", List.of(typeStructure));

    // then
    assertThat(repo.getTypes())
        .extracting(
            Type::getTypeInternalName,
            type -> type.getMethods().stream().map(Method::getMethodName).toList())
        .containsExactlyInAnyOrder(
            tuple(internalTypeName, List.of(internalMethodName)),
            tuple(externalTypeName, List.of(externalMethodName)));
  }

  /** 1️⃣ declared + super 충돌 */
  @Test
  @DisplayName("내부에 선언된 타입이 super로 등장하면 외부 타입으로 추가되지 않는다")
  void declared_and_super_conflict() {
    String parent = "com/example/Parent";
    String child = "com/example/Child";

    TypeStructure parentStructure =
        new TypeStructure(
            "Parent.java",
            RepoFileType.FILE,
            "",
            new TypeInfo(9, TypeKind.CLASS, parent, null, null, List.of()),
            List.of());

    TypeStructure childStructure =
        new TypeStructure(
            "Child.java",
            RepoFileType.FILE,
            "",
            new TypeInfo(9, TypeKind.CLASS, child, parent, null, List.of()),
            List.of());

    OpenSourceRepo repo =
        openSourceRepoService.createRepo("url", List.of(parentStructure, childStructure));

    assertThat(repo.getTypes())
        .extracting(Type::getTypeInternalName, Type::getTypeOrigin)
        .containsExactlyInAnyOrder(
            tuple(parent, TypeOrigin.INTERNAL), tuple(child, TypeOrigin.INTERNAL));
  }

  /** 2️⃣ declared + methodCall 충돌 */
  @Test
  @DisplayName("내부에 존재하는 타입을 methodCall로 호출해도 외부 타입으로 생성되지 않는다")
  void declared_and_methodCall_conflict() {
    String internalA = "com/example/A";
    String internalB = "com/example/B";

    MethodCallInfo call = MethodCallInfo.of(182, internalB, "bar", "()V", false);

    MethodStructure methodStructure =
        new MethodStructure(
            new MethodInfo(
                internalA,
                "foo",
                EnumSet.noneOf(MethodModifier.class),
                MethodDescriptor.from("()V"),
                null,
                null,
                null,
                null),
            List.of(call));

    TypeStructure structureA =
        new TypeStructure(
            "A.java",
            RepoFileType.FILE,
            "",
            new TypeInfo(9, TypeKind.CLASS, internalA, null, null, List.of()),
            List.of(methodStructure));

    TypeStructure structureB =
        new TypeStructure(
            "B.java",
            RepoFileType.FILE,
            "",
            new TypeInfo(9, TypeKind.CLASS, internalB, null, null, List.of()),
            List.of());

    OpenSourceRepo repo = openSourceRepoService.createRepo("url", List.of(structureA, structureB));

    assertThat(repo.getTypes())
        .extracting(Type::getTypeInternalName)
        .containsExactlyInAnyOrder(internalA, internalB);
  }

  /** 3️⃣ super + methodCall 동일 external → 1개만 생성 + method 유지 */
  @Test
  @DisplayName("super와 methodCall로 동시에 들어온 외부 타입은 하나로 병합되고 호출된 메서드를 가진다")
  void merge_super_and_methodCall() {

    String internal = "com/example/A";
    String external = "java/util/List";

    TypeInfo typeInfo = new TypeInfo(9, TypeKind.CLASS, internal, external, null, List.of());

    MethodCallInfo call = MethodCallInfo.of(INVOKE_INTERFACE, external, "size", "()I", true);

    MethodStructure methodStructure =
        new MethodStructure(
            new MethodInfo(
                internal,
                "foo",
                EnumSet.noneOf(MethodModifier.class),
                MethodDescriptor.from("()V"),
                null,
                null,
                null,
                null),
            List.of(call));

    TypeStructure structure =
        new TypeStructure("A.java", RepoFileType.FILE, "", typeInfo, List.of(methodStructure));

    OpenSourceRepo repo = openSourceRepoService.createRepo("url", List.of(structure));

    List<Type> externals =
        repo.getTypes().stream().filter(t -> t.getTypeInternalName().equals(external)).toList();

    assertThat(externals)
        .hasSize(1)
        .extracting(Type::getTypeInternalName)
        .containsExactly(external);
    assertThat(externals.get(0).getMethods())
        .extracting(Method::getMethodName)
        .containsExactly("size");
  }

  /** 4️⃣ methodCall 여러 개 → method 누적 */
  @Test
  @DisplayName("동일 외부 타입에 대해 여러 메서드가 호출되면 메서드는 누적된다")
  void accumulate_methods() {

    String internal = "com/example/A";
    String external = "java/util/List";

    MethodCallInfo call1 = MethodCallInfo.of(INVOKE_INTERFACE, external, "size", "()I", true);

    MethodCallInfo call2 = MethodCallInfo.of(INVOKE_INTERFACE, external, "isEmpty", "()Z", true);

    MethodStructure methodStructure =
        new MethodStructure(
            new MethodInfo(
                internal,
                "foo",
                EnumSet.noneOf(MethodModifier.class),
                MethodDescriptor.from("()V"),
                null,
                null,
                null,
                null),
            List.of(call1, call2));

    TypeStructure structure =
        new TypeStructure(
            "A.java",
            RepoFileType.FILE,
            "",
            new TypeInfo(9, TypeKind.CLASS, internal, null, null, List.of()),
            List.of(methodStructure));

    OpenSourceRepo repo = openSourceRepoService.createRepo("url", List.of(structure));

    Type externalType =
        repo.getTypes().stream()
            .filter(t -> t.getTypeInternalName().equals(external))
            .findFirst()
            .orElseThrow();

    assertThat(externalType.getMethods())
        .extracting(Method::getMethodName)
        .containsExactlyInAnyOrder("size", "isEmpty");
  }

  /** 5️⃣ 동일 methodName + descriptor → 중복 제거 */
  @Test
  @DisplayName("동일한 methodName과 descriptor를 가진 호출은 하나만 유지된다")
  void deduplicate_same_signature() {

    String internal = "com/example/A";
    String external = "java/util/List";

    MethodCallInfo call1 = MethodCallInfo.of(INVOKE_INTERFACE, external, "size", "()I", true);

    MethodCallInfo call2 = MethodCallInfo.of(INVOKE_INTERFACE, external, "size", "()I", true);

    MethodStructure methodStructure =
        new MethodStructure(
            new MethodInfo(
                internal,
                "foo",
                EnumSet.noneOf(MethodModifier.class),
                MethodDescriptor.from("()V"),
                null,
                null,
                null,
                null),
            List.of(call1, call2));

    TypeStructure structure =
        new TypeStructure(
            "A.java",
            RepoFileType.FILE,
            "",
            new TypeInfo(9, TypeKind.CLASS, internal, null, null, List.of()),
            List.of(methodStructure));

    OpenSourceRepo repo = openSourceRepoService.createRepo("url", List.of(structure));

    Type externalType =
        repo.getTypes().stream()
            .filter(t -> t.getTypeInternalName().equals(external))
            .findFirst()
            .orElseThrow();

    assertThat(externalType.getMethods()).hasSize(1);
  }
}
