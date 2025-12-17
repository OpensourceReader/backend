package com.opensourcereader.core.analysis.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.FieldInfo;
import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.PathAndType;
import com.opensourcereader.core.analysis.service.impl.JavaAstExtractor.ReceiverMethodName;

public class MethodFlowService {

  public static final String PATH_SEPARATOR = ".";

  // 여기보다 extractor를 먼저 가야겠네
  public List<PathAndMethod> extractPathAndMethod(String rawText, String targetMethodName) {
    Map<String, String> fieldInfos =
        JavaAstExtractor.extractField(rawText).stream()
            .collect(Collectors.toMap(FieldInfo::fieldName, FieldInfo::type));

    Map<String, List<String>> fieldInfoMethodCall =
        JavaAstExtractor.extractMethodCall(rawText, targetMethodName).stream()
            .filter(rm -> fieldInfos.containsKey(rm.receiver()))
            .collect(
                Collectors.groupingBy(
                    rm -> fieldInfos.get(rm.receiver()),
                    Collectors.mapping(ReceiverMethodName::methodName, Collectors.toList())));

    Map<String, String> pathAndTypes =
        JavaAstExtractor.extractPath(rawText, PATH_SEPARATOR).stream()
            .collect(Collectors.toMap(PathAndType::type, PathAndType::path));
    return fieldInfoMethodCall.entrySet().stream()
        .filter(entry -> pathAndTypes.containsKey(entry.getKey()))
        .flatMap(
            entry ->
                entry.getValue().stream()
                    .map(
                        methodName ->
                            new PathAndMethod(pathAndTypes.get(entry.getKey()), methodName)))
        .toList();
  }

  public record PathAndMethod(String path, String methodName) {}
}
