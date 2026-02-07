package com.opensourcereader.core.analysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;

@Configuration
public class JavaParserConfig {

  @Bean
  public JavaParser javaParser() {
    ParserConfiguration parserConfiguration =
        new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
    return new JavaParser(parserConfiguration);
  }
}
