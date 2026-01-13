package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.analysis.dto.callgraph.ClassInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.entity.repo.DeclaredType;
import com.opensourcereader.core.analysis.entity.repo.TypeKind;
import com.opensourcereader.core.analysis.entity.repo.TypeOrigin;
import com.opensourcereader.core.analysis.repository.DeclaredTypeRepository;
import com.opensourcereader.core.analysis.service.DeclaredTypeHierarchyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Transactional
@SpringBootTest
class DeclaredTypeHierarchyServiceTest {

  @Autowired private DeclaredTypeRepository declaredTypeRepository;

  @Autowired private DeclaredTypeHierarchyService service;

  private ClassStructure toClassStructure(
      String className, String superName, List<String> interfaceNames) {
    ClassInfo classInfo =
        new ClassInfo(0, TypeKind.CLASS, className, null, superName, interfaceNames);
    return new ClassStructure(classInfo, List.of());
  }

  @Test
  @DisplayName("타입 구조 입력이 비어있으면 아무 타입도 갱신하지 않는다")
  void resolveTypeHierarchy_emptyInput() {
    // given
    List<ClassStructure> classStructures = List.of();

    // when
    List<DeclaredType> result = service.resolveTypeHierarchy(classStructures);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("클래스가 인터페이스를 구현하면 implement edge를 연결한다")
  void resolveTypeHierarchy_classImplementsInterface() {
    // given
    String implementClassName = "ImplementClass";
    String interfaceName = "Interface";
    ClassStructure interfaceStructure = toClassStructure(interfaceName, null, List.of());
    ClassStructure classStructure =
        toClassStructure(implementClassName, null, List.of(interfaceName));
    DeclaredType interfaceI = DeclaredType.internal(interfaceStructure, List.of());
    DeclaredType classA = DeclaredType.internal(classStructure, List.of());

    declaredTypeRepository.saveAll(List.of(interfaceI, classA));

    // when
    List<ClassStructure> classStructures = List.of(interfaceStructure, classStructure);
    List<DeclaredType> result = service.resolveTypeHierarchy(classStructures);

    // then
    assertThat(declaredTypeRepository.findAll()).hasSize(2);
    assertThat(result)
        .extracting(DeclaredType::getImplementedInterfaces)
        .flatExtracting(edges -> edges)
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(interfaceName);
  }

  @Test
  @DisplayName("인터페이스가 다른 인터페이스를 확장하면 implement edge를 연결한다")
  void resolveTypeHierarchy_interfaceExtendsInterface() {
    // given
    String parentInterfaceName = "I";
    String childInterfaceName = "J";

    ClassStructure parentInterfaceStructure =
        toClassStructure(parentInterfaceName, null, List.of());
    ClassStructure childInterfaceStructure =
        toClassStructure(childInterfaceName, null, List.of(parentInterfaceName));

    DeclaredType parentInterface = DeclaredType.internal(parentInterfaceStructure, List.of());
    DeclaredType childInterface = DeclaredType.internal(childInterfaceStructure, List.of());

    declaredTypeRepository.saveAll(List.of(parentInterface, childInterface));

    // when
    List<ClassStructure> classStructures =
        List.of(parentInterfaceStructure, childInterfaceStructure);
    service.resolveTypeHierarchy(classStructures);

    // then
    assertThat(declaredTypeRepository.findAll()).hasSize(2);

    DeclaredType savedChild =
        declaredTypeRepository.findByTypeInternalName(childInterfaceName).orElseThrow();
    assertThat(savedChild.getImplementedInterfaces())
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(parentInterfaceName);
  }

  @Test
  @DisplayName("클래스가 다른 클래스를 상속하면 super 타입으로 연결한다(class B extends A)")
  void resolveTypeHierarchy_classExtendsClass() {
    // given
    String superClassName = "A";
    String subClassName = "B";

    ClassStructure superStructure = toClassStructure(superClassName, null, List.of());
    ClassStructure subStructure = toClassStructure(subClassName, superClassName, List.of());
    DeclaredType superType = DeclaredType.internal(superStructure, List.of());
    DeclaredType subType = DeclaredType.internal(subStructure, List.of());

    declaredTypeRepository.saveAll(List.of(superType, subType));

    // when
    List<ClassStructure> classStructures = List.of(superStructure, subStructure);
    service.resolveTypeHierarchy(classStructures);

    // then
    assertThat(declaredTypeRepository.findAll()).hasSize(2);

    DeclaredType savedSub =
        declaredTypeRepository.findByTypeInternalName(subClassName).orElseThrow();
    assertThat(savedSub.getSuperType()).isNotNull();
    assertThat(savedSub.getSuperType().getTypeInternalName()).isEqualTo(superClassName);
  }

  @Test
  @DisplayName("상위 타입이나 인터페이스가 없으면 외부 타입으로 해석한다")
  void resolveTypeHierarchy_externalTypes() {
    // given
    String className = "A";
    String externalSuper = "external/Super";
    String externalInterface1 = "externalInterface/Interface1";
    String externalInterface2 = "externalInterface/Interface2";

    ClassStructure classStructure =
        toClassStructure(className, externalSuper, List.of(externalInterface1, externalInterface2));
    DeclaredType internalA = DeclaredType.internal(classStructure, List.of());
    declaredTypeRepository.save(internalA);

    // when
    service.resolveTypeHierarchy(List.of(classStructure));

    // then
    assertThat(declaredTypeRepository.findAll()).hasSize(4);
    DeclaredType savedResult =
        declaredTypeRepository.findByTypeInternalName(className).orElseThrow();

    // 1. superType이 external로 들어갔는지
    assertThat(savedResult.getSuperType()).isNotNull();
    assertThat(savedResult.getSuperType().getTypeInternalName()).isEqualTo(externalSuper);
    assertThat(savedResult.getSuperType().getTypeOrigin()).isEqualTo(TypeOrigin.EXTERNAL);

    // 2. implementedInterfaces에 external 두 개가 들어갔는지
    assertThat(savedResult.getImplementedInterfaces())
        .extracting(edge -> edge.getInterfaceType().getTypeInternalName())
        .containsExactlyInAnyOrder(externalInterface1, externalInterface2);
  }
}
