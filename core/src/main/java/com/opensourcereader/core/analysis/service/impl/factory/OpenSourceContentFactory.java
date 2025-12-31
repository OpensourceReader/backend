package com.opensourcereader.core.analysis.service.impl.factory;

import static com.opensourcereader.core.analysis.entity.repo.ContentType.getContentTypeFromTypeNumber;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.dto.callgraph.ClassStructure;
import com.opensourcereader.core.analysis.dto.callgraph.CodeMethodExtractResult;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepo;
import com.opensourcereader.core.analysis.entity.repo.OpenSourceRepoContent;
import com.opensourcereader.core.analysis.infra.parser.SourceCodeParser;
import com.opensourcereader.core.analysis.service.impl.OpenSourceCodeMethodExtractor;

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
    Map<String, ClassStructure> structures =
        classStructures.stream()
            .collect(Collectors.toMap(cs -> cs.classInfo().className(), cs -> cs));
    return sourFileInfos.stream()
        .filter(sourFile -> getContentTypeFromTypeNumber(sourFile.typeNumber()).isSupported())
        .map(sourFile -> getOpenSourceRepoContent(structures, opensourceRepo, sourFile))
        .toList();
  }

  private OpenSourceRepoContent getOpenSourceRepoContent(
      Map<String, ClassStructure> structures,
      OpenSourceRepo opensourceRepo,
      OpenSourceFileInfo sourFileInfo) {
    String sourceCodeClassName =
        sourceCodeParser.extractClassName(sourFileInfo.rawText()).replace('.', '/');
    ClassStructure classStructure = structures.get(sourceCodeClassName);
    List<CodeMethodExtractResult> methods =
        openSourceCodeMethodExtractor.extract(sourFileInfo, classStructure);
    return OpenSourceRepoContent.of(sourFileInfo, classStructure, methods, opensourceRepo);
  }
}
