package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.dto.TypeStructureMeta;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.Type;
import com.opensourcereader.core.analysis.entity.file.RepoEntryType;
import com.opensourcereader.core.analysis.entity.type.TypeKind;
import com.opensourcereader.core.analysis.entity.type.TypeOrigin;
import com.opensourcereader.core.analysis.repository.OpenSourceRepoRepository;
import com.opensourcereader.core.analysis.repository.TypeRepository;
import com.opensourcereader.core.analysis.service.TypeHierarchyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class TypeHierarchyServiceTest {

  @Autowired private TypeRepository typeRepository;
  @Autowired private OpenSourceRepoRepository openSourceRepoRepository;

  @Autowired private TypeHierarchyService service;

  private TypeStructure toTypeStructureWithoutMethod(
      String className, String superName, List<String> interfaceNames) {
    TypeInfo typeInfo = new TypeInfo(0, TypeKind.CLASS, className, null, superName, interfaceNames);
    return new TypeStructure(className + "path", RepoEntryType.FILE, null, typeInfo, List.of());
  }

  @Test
  @DisplayName("타입 구조 입력 및 repoId가 주어지지 않으며느 아무 타입도 갱신하지 않는다")
  void resolve_emptyInput() {
    // given
    List<TypeStructureMeta> typeStructureMetas = List.of();

    // when
    List<Type> result = service.resolve(null, typeStructureMetas);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("클래스가 인터페이스를 구현하면 implement edge를 연결한다")
  void resolve_classImplementsInterface() {
    // given
    String interfaceName = "Interface";
    TypeStructure interfaceStructure = toTypeStructureWithoutMethod(interfaceName, null, List.of());

    String implementClassName = "ImplementClass";
    TypeStructure implementedClassStructure =
        toTypeStructureWithoutMethod(implementClassName, null, List.of(interfaceName));

    OpenSourceRepo openSourceRepo =
        openSourceRepoRepository.save(
            OpenSourceRepo.of("new-Uri", List.of(interfaceStructure, implementedClassStructure)));

    // when
    List<TypeStructureMeta> typeStructureMetas =
        TypeStructureMeta.from(List.of(interfaceStructure, implementedClassStructure));
    List<Type> result = service.resolve(openSourceRepo.getId(), typeStructureMetas);

    // then
    assertThat(typeRepository.findAll()).hasSize(2);
    assertThat(result)
        .extracting(Type::getImplementedInterfaces)
        .flatExtracting(edges -> edges)
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(interfaceName);
  }

  @Test
  @DisplayName("인터페이스가 다른 인터페이스를 확장하면 ImplementedInterface로 연결한다")
  void resolve_interfaceExtendsInterface() {
    // given
    String parentInterfaceName = "I";
    TypeStructure parentInterfaceStructure =
        toTypeStructureWithoutMethod(parentInterfaceName, null, List.of());

    String childInterfaceName = "J";
    TypeStructure childInterfaceStructure =
        toTypeStructureWithoutMethod(childInterfaceName, null, List.of(parentInterfaceName));

    OpenSourceRepo openSourceRepo =
        openSourceRepoRepository.save(
            OpenSourceRepo.of(
                "new-Uri", List.of(parentInterfaceStructure, childInterfaceStructure)));

    // when
    List<TypeStructureMeta> structureMetas =
        TypeStructureMeta.from(List.of(parentInterfaceStructure, childInterfaceStructure));
    service.resolve(openSourceRepo.getId(), structureMetas);

    // then
    assertThat(typeRepository.findAll()).hasSize(2);

    Type savedChild =
        typeRepository
            .findByRepoAndTypeInternalName(openSourceRepo.getId(), childInterfaceName)
            .orElseThrow();
    assertThat(savedChild.getImplementedInterfaces())
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(parentInterfaceName);
  }

  @Test
  @DisplayName("클래스가 다른 클래스를 상속하면 super 타입으로 연결한다(class B extends A)")
  void resolve_classExtendsClass() {
    // given
    String superClassName = "A";
    String subClassName = "B";

    TypeStructure superStructure = toTypeStructureWithoutMethod(superClassName, null, List.of());
    TypeStructure subStructure =
        toTypeStructureWithoutMethod(subClassName, superClassName, List.of());

    OpenSourceRepo openSourceRepo =
        openSourceRepoRepository.save(
            OpenSourceRepo.of("new-Uri", List.of(superStructure, subStructure)));

    // when
    service.resolve(
        openSourceRepo.getId(), TypeStructureMeta.from(List.of(superStructure, subStructure)));

    // then
    assertThat(typeRepository.findAll()).hasSize(2);

    Type savedSub =
        typeRepository
            .findByRepoAndTypeInternalName(openSourceRepo.getId(), subClassName)
            .orElseThrow();
    assertThat(savedSub.getSuperType()).isNotNull();
    assertThat(savedSub.getSuperType().getTypeInternalName()).isEqualTo(superClassName);
  }

  @Test
  @DisplayName("상위 타입이나 인터페이스가 없으면 외부 타입으로 해석한다")
  void resolve_externalTypes() {
    // given
    String className = "A";
    String externalSuper = "external/Super";
    String externalInterface1 = "externalInterface/Interface1";
    String externalInterface2 = "externalInterface/Interface2";

    TypeStructure typeStructure =
        toTypeStructureWithoutMethod(
            className, externalSuper, List.of(externalInterface1, externalInterface2));
    OpenSourceRepo openSourceRepo =
        openSourceRepoRepository.save(OpenSourceRepo.of("new-Uri", List.of(typeStructure)));

    // when
    service.resolve(openSourceRepo.getId(), TypeStructureMeta.from(List.of(typeStructure)));

    // then
    assertThat(typeRepository.findAll()).hasSize(4);
    Type savedResult =
        typeRepository
            .findByRepoAndTypeInternalName(openSourceRepo.getId(), className)
            .orElseThrow();

    // 1. superType이 external로 들어갔는지
    assertThat(savedResult.getSuperType()).isNotNull();
    assertThat(savedResult.getSuperType().getTypeInternalName()).isEqualTo(externalSuper);
    assertThat(savedResult.getSuperType().getTypeOrigin()).isEqualTo(TypeOrigin.EXTERNAL);

    // 2. implementedInterfaces에 external 두 개가 들어갔는지
    assertThat(savedResult.getImplementedInterfaces())
        .extracting(
            edge -> edge.getInterfaceType().getTypeInternalName(),
            edge -> edge.getInterfaceType().getTypeOrigin())
        .containsExactlyInAnyOrder(
            tuple(externalInterface1, TypeOrigin.EXTERNAL),
            tuple(externalInterface2, TypeOrigin.EXTERNAL));
  }

  @Test
  @DisplayName("resolve는 다른 repo에 존재하는 타입을 가져오지 않고, 현재 repo 기준으로 타입을 생성/연결한다")
  void resolve_doesNotReuseTypesFromOtherRepo() {
    // given
    String typeA = "A";
    TypeStructure repo1A = toTypeStructureWithoutMethod(typeA, null, List.of());
    OpenSourceRepo repoA =
        openSourceRepoRepository.save(OpenSourceRepo.of("repo1", List.of(repo1A)));

    // repoB에는 B extends A만 존재 (A는 repoB 구조에는 없음)
    String typeB = "B";
    TypeStructure repo2B = toTypeStructureWithoutMethod(typeB, typeA, List.of());
    OpenSourceRepo repoB =
        openSourceRepoRepository.save(OpenSourceRepo.of("repo2", List.of(repo2B)));

    // when : B에서 외부 A를 새로 생성해서 연결을함
    service.resolve(repoB.getId(), TypeStructureMeta.from(List.of(repo2B)));

    // then
    // B의 super는 미지의 외부 A입니다.
    Type repoBSavedB =
        typeRepository.findByRepoAndTypeInternalName(repoB.getId(), typeB).orElseThrow();

    assertThat(repoBSavedB.getSuperType()).isNotNull();
    assertThat(repoBSavedB.getSuperType().getTypeInternalName()).isEqualTo(typeA);
    assertThat(repoBSavedB.getSuperType().getTypeOrigin()).isEqualTo(TypeOrigin.EXTERNAL);

    Type repoBSavedA =
        typeRepository.findByRepoAndTypeInternalName(repoB.getId(), typeA).orElseThrow();

    Type repoASavedA =
        typeRepository.findByRepoAndTypeInternalName(repoA.getId(), typeA).orElseThrow();

    // 가장 중요한 검증: repo2의 B가 repo1의 A를 참조하면 안 됨
    assertThat(repoBSavedB.getSuperType().getId()).isEqualTo(repoBSavedA.getId());
    assertThat(repoBSavedB.getSuperType().getId()).isNotEqualTo(repoASavedA.getId());

    // sanity: 동일 typeInternalName A가 repo별로 각각 존재해야 함
    assertThat(
            typeRepository.findAll().stream()
                .filter(t -> t.getTypeInternalName().equals(typeA))
                .toList())
        .hasSize(2);
  }
}
