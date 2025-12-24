package com.opensourcereader.core.analysis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;

@Configuration
public class JavaParserConfig {

  @Bean
  public JavaParser javaParser() {
    ParserConfiguration parserConfiguration =
        new ParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_17);
    return new JavaParser(parserConfiguration);
  }
}
