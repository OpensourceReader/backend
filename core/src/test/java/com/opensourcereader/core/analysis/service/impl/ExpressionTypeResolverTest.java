package com.opensourcereader.core.analysis.service.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.Expression;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpressionTypeResolverTest {

  private final ExpressionTypeResolver expressionTypeResolver = new ExpressionTypeResolver();

  @DisplayName("")
  @Test
  void getTypeToString() {
    // given
    Expression expression = resolve("Rate.of(10)");

    // when
    String typeToString = expressionTypeResolver.getTypeToString(expression);

    // then
    Assertions.assertThat(typeToString).isNotNull();
  }

  private Expression resolve(String exprSource) {
    return StaticJavaParser.parseExpression(exprSource);
  }
}
