package com.opensourcereader.core.analysis.domain.service.hierarchy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.util.List;
import java.util.Objects;

import com.opensourcereader.core.analysis.domain.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.domain.entity.Type;
import com.opensourcereader.core.analysis.domain.entity.TypeImplementation;
import com.opensourcereader.core.analysis.domain.entity.factory.ExternalTypeStructureFactory;
import com.opensourcereader.core.analysis.domain.entity.factory.OpenSourceRepoFactory;
import com.opensourcereader.core.analysis.domain.entity.file.RepoFileType;
import com.opensourcereader.core.analysis.domain.entity.type.TypeKind;
import com.opensourcereader.core.analysis.domain.entity.type.TypeOrigin;
import com.opensourcereader.core.analysis.dto.TypeInfo;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InheritanceLinkerTest {

  private InheritanceLinker service = new InheritanceLinker();
  OpenSourceRepoFactory openSourceRepoFactory =
      new OpenSourceRepoFactory(new InheritanceLinker(), new ExternalTypeStructureFactory());

  private TypeStructure toTypeStructureWithoutMethod(
      String className, String superName, List<String> interfaceNames) {
    TypeInfo typeInfo = new TypeInfo(0, TypeKind.CLASS, className, null, superName, interfaceNames);
    return new TypeStructure(className + "path", RepoFileType.FILE, null, typeInfo, List.of());
  }

  @Test
  @DisplayName("타입 구조 입력 및 repoId가 주어지지 않으며느 아무 타입도 갱신하지 않는다")
  void resolve_emptyInput() {
    // given
    List<TypeStructure> typeStructures = List.of();

    // when
    OpenSourceRepo openSourceRepo = openSourceRepoFactory.create("", List.of());
    service.resolve(openSourceRepo.getTypes(), typeStructures);

    // then
    assertThat(openSourceRepo.getTypes()).isEmpty();
  }

  @Test
  @DisplayName("클래스가 인터페이스를 구현하면, 서로 연결한다")
  void resolve_classImplementsInterface() {
    // given
    String interfaceName = "Interface";
    TypeStructure interfaceStructure = toTypeStructureWithoutMethod(interfaceName, null, List.of());

    String implementClassName = "ImplementClass";
    TypeStructure implementedClassStructure =
        toTypeStructureWithoutMethod(implementClassName, null, List.of(interfaceName));
    OpenSourceRepo openSourceRepo =
        openSourceRepoFactory.create(
            "new-Uri", List.of(interfaceStructure, implementedClassStructure));

    // when
    service.resolve(
        openSourceRepo.getTypes(), List.of(interfaceStructure, implementedClassStructure));

    // then
    assertThat(openSourceRepo.getTypes()).hasSize(2);
    assertThat(openSourceRepo.getTypes())
        .extracting(Type::getImplementedInterfaces)
        .flatExtracting(edges -> edges)
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(interfaceName);
  }

  @Test
  @DisplayName("인터페이스가 다른 인터페이스를 확장해도, ImplementedInterface로 연결한다")
  void resolve_interfaceExtendsInterface() {
    // given
    String parentInterfaceName = "InterfaceParent";
    TypeStructure parentInterfaceStructure =
        toTypeStructureWithoutMethod(parentInterfaceName, null, List.of());

    String childInterfaceName = "InterfaceChild";
    TypeStructure childInterfaceStructure =
        toTypeStructureWithoutMethod(childInterfaceName, null, List.of(parentInterfaceName));

    OpenSourceRepo openSourceRepo =
        openSourceRepoFactory.create(
            "new-Uri", List.of(parentInterfaceStructure, childInterfaceStructure));

    // when
    service.resolve(
        openSourceRepo.getTypes(), List.of(parentInterfaceStructure, childInterfaceStructure));

    // then
    assertThat(openSourceRepo.getTypes()).hasSize(2);
    List<TypeImplementation> typeImplementations =
        openSourceRepo.getTypes().stream()
            .flatMap(type -> type.getImplementedInterfaces().stream())
            .toList();
    assertThat(typeImplementations)
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(parentInterfaceName);
  }

  @Test
  @DisplayName("클래스가 다른 클래스를 상속하면 super 타입으로 연결한다(class B extends A)")
  void resolve_classExtendsClass() {
    // given
    String superClassName = "A";
    String childClassName = "B";

    TypeStructure superStructure = toTypeStructureWithoutMethod(superClassName, null, List.of());
    TypeStructure subStructure =
        toTypeStructureWithoutMethod(childClassName, superClassName, List.of());
    OpenSourceRepo openSourceRepo =
        openSourceRepoFactory.create("new-Uri", List.of(superStructure, subStructure));

    // when
    service.resolve(openSourceRepo.getTypes(), List.of(superStructure, subStructure));

    // then
    assertThat(openSourceRepo.getTypes()).hasSize(2);
    List<String> superTypeNames =
        openSourceRepo.getTypes().stream()
            .map(Type::getSuperType)
            .filter(Objects::nonNull)
            .map(Type::getTypeInternalName)
            .toList();
    assertThat(superTypeNames).containsExactlyInAnyOrder(superClassName);
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
    OpenSourceRepo openSourceRepo = openSourceRepoFactory.create("new-Uri", List.of(typeStructure));

    // when
    service.resolve(openSourceRepo.getTypes(), List.of(typeStructure));

    // then
    assertThat(openSourceRepo.getTypes()).hasSize(4);
    Type innerClassType =
        openSourceRepo.getTypes().stream()
            .filter(type -> type.getTypeInternalName().equals(className))
            .toList()
            .get(0);

    // 1. superType이 external로 들어갔는지
    assertThat(innerClassType.getSuperType()).isNotNull();
    assertThat(innerClassType.getSuperType().getTypeInternalName()).isEqualTo(externalSuper);
    assertThat(innerClassType.getSuperType().getTypeOrigin()).isEqualTo(TypeOrigin.EXTERNAL);

    // 2. implementedInterfaces에 external 두 개가 들어갔는지
    assertThat(innerClassType.getImplementedInterfaces())
        .extracting(
            edge -> edge.getInterfaceType().getTypeInternalName(),
            edge -> edge.getInterfaceType().getTypeOrigin())
        .containsExactlyInAnyOrder(
            tuple(externalInterface1, TypeOrigin.EXTERNAL),
            tuple(externalInterface2, TypeOrigin.EXTERNAL));
  }
}
