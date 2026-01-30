package com.opensourcereader.core.analysis.infra.parser;

import static com.opensourcereader.core.analysis.entity.shared.NameSeparators.PACKAGE_SEPARATOR;
import static com.opensourcereader.core.analysis.entity.shared.NameSeparators.PATH_SEPARATOR;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.opensourcereader.core.analysis.entity.method.CodeMethodSignature;
import com.opensourcereader.core.analysis.entity.repo.Extension;
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

    Map<CodeMethodSignature, SourceCodeParseResult> methodsBySignature =
        sourceCodeParser.extractCodeMethods(file.rawText()).stream()
            .collect(
                Collectors.toMap(
                    CodeMethodSignature::of, r -> r, (a, b) -> a // 충돌 정책(원하면 throw로 바꿔도 됨)
                    ));

    return new ParsedSourceFile(classInternalName, methodsBySignature);
  }
}
