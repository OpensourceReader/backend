package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.ParsedSourceFile;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.infra.SourceFileParseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceContentFactory {

  private final SourceFileParseService sourceFileParseService;

  public List<OpenSourceRepoContent> create(
      List<OpenSourceFileInfo> sourFileInfos,
      List<ClassStructure> classStructures,
      OpenSourceRepo opensourceRepo) {
    Map<String, ClassStructure> structures =
        classStructures.stream()
            .collect(Collectors.toMap(cs -> cs.classInfo().className(), cs -> cs));

    return sourFileInfos.stream()
        .filter(sourFile -> sourFile.repoEntryType().isSupported())
        .map(sourFile -> toRepoContent(opensourceRepo, sourFile, structures))
        .toList();
  }

  // extractMethod할떄 마지막 리턴타입에서, String 말고, com.ex. 같은 커스텀 타입도 검증 필요 -> 클래스 분리 필요
  private OpenSourceRepoContent toRepoContent(
      OpenSourceRepo opensourceRepo,
      OpenSourceFileInfo sourFile,
      Map<String, ClassStructure> structures) {
    ParsedSourceFile parsedFile = sourceFileParseService.parse(sourFile);
    ClassStructure structure = structures.get(parsedFile.classInternalName());
    List<CodeMethodExtractResult> methods = extractMethod(structure, parsedFile);

    return OpenSourceRepoContent.of(sourFile, structure.classInfo(), methods, opensourceRepo);
  }

  private List<CodeMethodExtractResult> extractMethod(
      ClassStructure classStructure, ParsedSourceFile parsedSourceFile) {
    if (classStructure == null || parsedSourceFile == null) {
      return List.of();
    }

    return classStructure.methods().stream()
        .map(
            m -> {
              CodeMethodSignature sig = CodeMethodSignature.of(m.declaredMethodInfo());
              return CodeMethodExtractResult.of(
                  m.declaredMethodInfo(), parsedSourceFile.methodsBySignature().get(sig));
            })
        .toList();
  }
}
