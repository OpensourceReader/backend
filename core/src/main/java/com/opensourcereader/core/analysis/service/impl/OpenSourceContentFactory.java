package com.opensourcereader.core.analysis.service.impl;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.ParsedSourceFile;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.dto.callgraph.SourceCodeParseResult;
import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.infra.SourceFileParseService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        .filter(sourFile -> sourFile.contentType().isSupported())
        .map(sourFile -> {
          ParsedSourceFile parsed = sourceFileParseService.parse(sourFile);
          ClassStructure classStructure = structures.get(parsed.classInternalName());
          List<CodeMethodExtractResult> methods = extract(classStructure, parsed);

          return OpenSourceRepoContent.of(sourFile, classStructure, methods, opensourceRepo);
        })
        .toList();
  }

  private List<CodeMethodExtractResult> extract(
      ClassStructure classStructure,
      ParsedSourceFile parsedSourceFile
  ) {
    if (classStructure == null) {
      return List.of();
    }

    return classStructure.methods().stream()
        .map(m -> {
          CodeMethodSignature sig = CodeMethodSignature.of(m.declaredMethodInfo());
          return CodeMethodExtractResult.of(m.declaredMethodInfo(),
              parsedSourceFile.methodsBySignature().get(sig));
        })
        .toList();
  }
}
