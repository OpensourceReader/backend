package com.opensourcereader.core.analysis.infra.parser;

import static com.opensourcereader.core.analysis.domain.entity.file.FileNameSeparators.PACKAGE_SEPARATOR;
import static com.opensourcereader.core.analysis.domain.entity.file.FileNameSeparators.PATH_SEPARATOR;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.domain.entity.file.Extension;
import com.opensourcereader.core.analysis.domain.entity.method.MethodSignature;
import com.opensourcereader.core.analysis.infra.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.dto.ParsedSourceFile;
import com.opensourcereader.core.analysis.infra.dto.SourceCodeParseResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SourceFileParser {

  private final SourceCodeParser sourceCodeParser;

  public ParsedSourceFile parse(OpenSourceFileInfo file) {
    if (!Extension.isJavaFile(file.path())) {
      return ParsedSourceFile.empty();
    }

    String classInternalName =
        sourceCodeParser
            .extractClassName(file.rawText())
            .replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);

    Map<MethodSignature, SourceCodeParseResult> methodsBySignature =
        sourceCodeParser.extractCodeMethods(file.rawText()).stream()
            .collect(
                Collectors.toMap(
                    MethodSignature::of, r -> r, (a, b) -> a // 충돌 정책(원하면 throw로 바꿔도 됨)
                    ));

    return new ParsedSourceFile(classInternalName, methodsBySignature);
  }
}
