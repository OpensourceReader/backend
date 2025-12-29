package com.opensourcereader.core.analysis.service.impl;

import static com.opensourcereader.core.analysis.entity.ContentType.getContentTypeFromTypeNumber;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.infra.SourceCodeParser;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenSourceContentFactory {

  private final SourceCodeParser sourceCodeParser;
  private final OpenSourceCodeMethodExtractor openSourceCodeMethodExtractor;

  public List<OpenSourceRepoContent> create(
      List<OpenSourceFileInfo> sourFileInfos,
      List<ClassStructure> classStructures,
      OpenSourceRepo opensourceRepo) {
    Map<String, ClassStructure> structures = getByClassName(classStructures);
    return sourFileInfos.stream()
        .filter(sourFile -> getContentTypeFromTypeNumber(sourFile.typeNumber()).isSupported())
        .map(sourFile -> getOpenSourceRepoContent(structures, opensourceRepo, sourFile))
        .toList();
  }

  private Map<String, ClassStructure> getByClassName(List<ClassStructure> classStructures) {
    return classStructures.stream()
        .collect(Collectors.toMap(cs -> cs.classInfo().className(), cs -> cs));
  }

  private OpenSourceRepoContent getOpenSourceRepoContent(
      Map<String, ClassStructure> structures,
      OpenSourceRepo opensourceRepo,
      OpenSourceFileInfo sourFileInfo) {
    ClassStructure classStructure =
        structures.get(sourceCodeParser.extractClassName(sourFileInfo.rawText()));
    List<CodeMethodExtractResult> methods =
        openSourceCodeMethodExtractor.extract(sourFileInfo, classStructure);
    return OpenSourceRepoContent.of(sourFileInfo, methods, opensourceRepo);
  }
}
