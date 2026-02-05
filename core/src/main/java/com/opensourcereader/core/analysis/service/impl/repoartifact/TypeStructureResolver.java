package com.opensourcereader.core.analysis.service.impl.repoartifact;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.MethodStructure;
import com.opensourcereader.core.analysis.dto.TypeStructure;
import com.opensourcereader.core.analysis.infra.dto.ByteCodeClassStructure;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.dto.ParsedSourceFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TypeStructureResolver {

  // 마지막 리턴타입에서, String 말고, com.ex. 같은 커스텀 타입도 검증 필요 -> 클래스 분리 필요
  public TypeStructure resolve(
      OpenSourceFileInfo sourceFile,
      ParsedSourceFile parsedFile,
      Map<String, ByteCodeClassStructure> byteCodeStructures) {
    if (parsedFile == null || parsedFile.typeInternalName() == null) {
      return TypeStructure.of(sourceFile, null, null);
    }
    ByteCodeClassStructure structure = byteCodeStructures.get(parsedFile.typeInternalName());
    if (structure == null) {
      log.debug("No bytecode found for {}", parsedFile.typeInternalName());
      return TypeStructure.of(sourceFile, null, null);
    }
    return TypeStructure.of(
        sourceFile, structure.typeInfo(), resolveMethods(structure, parsedFile));
  }

  private List<MethodStructure> resolveMethods(
      ByteCodeClassStructure byteCodeClassStructure, ParsedSourceFile parsedSourceFile) {
    if (byteCodeClassStructure == null || parsedSourceFile == null) {
      return List.of();
    }
    return byteCodeClassStructure.methods().stream()
        .map(methodStructure -> MethodStructure.of(methodStructure, parsedSourceFile))
        .toList();
  }
}
