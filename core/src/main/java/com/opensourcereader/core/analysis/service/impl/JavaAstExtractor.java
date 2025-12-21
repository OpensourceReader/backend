package com.opensourcereader.core.analysis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JavaAstExtractor {

  public static final String PATH_SEPARATOR = ".";
  private final ExpressionTypeResolver expressionTypeResolver;

  // 축소필요
  public List<PathAndMethodSignature> extractOutgoingPathAndMethod(
      String rawText, String targetMethodName) {
    Map<String, String> fieldInfos =
        extractField(rawText).stream()
            .collect(Collectors.toMap(FieldInfo::fieldName, FieldInfo::type));

    Map<String, List<String>> fieldInfoMethodCall =
        extractMethodCall(rawText, targetMethodName).stream()
            .filter(rm -> fieldInfos.containsKey(rm.receiver()))
            .collect(
                Collectors.groupingBy(
                    rm -> fieldInfos.get(rm.receiver()),
                    Collectors.mapping(
                        ReceiverAndMethodSignature::methodSignature, Collectors.toList())));

    Map<String, String> pathAndTypes =
        extractPath(rawText, PATH_SEPARATOR).stream()
            .collect(Collectors.toMap(PathAndType::type, PathAndType::path));
    return fieldInfoMethodCall.entrySet().stream()
        .filter(entry -> pathAndTypes.containsKey(entry.getKey()))
        .flatMap(
            entry ->
                entry.getValue().stream()
                    .map(
                        methodName ->
                            new PathAndMethodSignature(
                                pathAndTypes.get(entry.getKey()).replace('.', '/') + ".java",
                                methodName)))
        .toList();
  }

  // 만약에 필드에 기본타입이 있으면 포함안되게 해야함 -> 이거는 비즈니스 로직

  public List<FieldInfo> extractField(String rawText) {
    List<FieldInfo> result = new ArrayList<>();
    CompilationUnit cu = StaticJavaParser.parse(rawText);
    for (FieldDeclaration fieldDeclaration : cu.findAll(FieldDeclaration.class)) {
      String type = fieldDeclaration.getElementType().asString();
      for (VariableDeclarator variable : fieldDeclaration.getVariables()) {
        String fieldName = variable.getNameAsString();
        result.add(new FieldInfo(type, fieldName));
      }
    }
    return result;
  }

  // 만약 static이면 .바로 이전걸 뽑아서 넣어야함 -> 이것도 비즈니스 로직
  // 추가 분석 필요
  public List<PathAndType> extractPath(String rawText, String pathSeparator) {
    CompilationUnit cu = StaticJavaParser.parse(rawText);

    List<PathAndType> result = new ArrayList<>();
    for (ImportDeclaration importDeclaration : cu.getImports()) {
      String fullPath = importDeclaration.getNameAsString();

      boolean isStatic = importDeclaration.isStatic();
      boolean isAsterisk = importDeclaration.isAsterisk(); // 와일드카드
      if (isStatic) {
        int lastDotIndex = fullPath.lastIndexOf(pathSeparator);
        fullPath = fullPath.substring(0, lastDotIndex + 1);
      }

      result.add(
          new PathAndType(
              fullPath, fullPath.substring(fullPath.lastIndexOf(pathSeparator) + 1), isAsterisk));
    }

    return result;
  }

  // import static 예외처리 필요
  public List<ReceiverAndMethodSignature> extractMethodCall(String rawText, String methodName) {
    CompilationUnit compilationUnit = StaticJavaParser.parse(rawText);

    List<ReceiverAndMethodSignature> result = new ArrayList<>();
    for (MethodDeclaration methodDeclaration : compilationUnit.findAll(MethodDeclaration.class)) {
      if (!methodDeclaration.getNameAsString().equals(methodName)) {
        continue;
      }
      for (MethodCallExpr methodCall : methodDeclaration.findAll(MethodCallExpr.class)) {
        String receiver = methodCall.getScope().map(Expression::toString).orElse(null);
        List<String> paramTypes = getParameterTypeFromArgument(methodCall);
        String methodSignature = methodCall.getNameAsString() + String.join(".", paramTypes);
        result.add(new ReceiverAndMethodSignature(receiver, methodSignature));
      }
    }

    return result;
  }

  public List<String> getParameterTypeFromArgument(MethodCallExpr call) {
    List<String> argTypes = new ArrayList<>();
    for (Expression argument : call.getArguments()) {
      argTypes.add(expressionTypeResolver.getTypeToString(argument));
      //      args.add(argument.toString());
    }
    return argTypes;
  }

  public record PathAndMethodSignature(String path, String methodSignature) {}

  public record ReceiverAndMethodSignature(String receiver, String methodSignature) {}

  public record PathAndType(String path, String type, boolean isAsterisk) {}

  public record FieldInfo(String type, String fieldName) {}
}
