package com.opensourcereader.core.analysis.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.opensourcereader.core.analysis.entity.shared.Extension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExtensionTest {

  @Test
  @DisplayName("확장자가 java 이면 JAVA를 반환한다 (대소문자 무시)")
  void resolveExtension_java() {
    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(Extension.resolveExtension("Test.java")).isEqualTo(Extension.JAVA);
          softly.assertThat(Extension.resolveExtension("Test.JAVA")).isEqualTo(Extension.OTHER);
          softly.assertThat(Extension.resolveExtension("Test.JaVa")).isEqualTo(Extension.OTHER);
        });
  }

  @Test
  @DisplayName("여러 확장자를 올바르게 매핑한다")
  void resolveExtension_various() {
    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(Extension.resolveExtension("config.yml")).isEqualTo(Extension.YML);
          softly.assertThat(Extension.resolveExtension("config.yaml")).isEqualTo(Extension.YAML);
          softly.assertThat(Extension.resolveExtension("script.kts")).isEqualTo(Extension.KTS);
          softly.assertThat(Extension.resolveExtension("schema.sql")).isEqualTo(Extension.SQL);
          softly.assertThat(Extension.resolveExtension("readme.md")).isEqualTo(Extension.MD);
          softly.assertThat(Extension.resolveExtension("image.png")).isEqualTo(Extension.PNG);
          softly.assertThat(Extension.resolveExtension("vector.svg")).isEqualTo(Extension.SVG);
        });
  }

  @Test
  @DisplayName("알 수 없는 확장자는 OTHER를 반환한다")
  void resolveExtension_other() {
    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(Extension.resolveExtension("file.unknown")).isEqualTo(Extension.OTHER);
          softly.assertThat(Extension.resolveExtension("file.exe")).isEqualTo(Extension.OTHER);
        });
  }

  @Test
  @DisplayName("확장자가 없는 파일은 OTHER를 반환한다")
  void resolveExtension_noExtension() {
    assertThat(Extension.resolveExtension("README")).isEqualTo(Extension.OTHER);
  }

  @Test
  @DisplayName("경로에 점이 여러 개 있어도 마지막 확장자 기준으로 판단한다")
  void resolveExtension_multipleDots() {
    SoftAssertions.assertSoftly(
        softly -> {
          softly
              .assertThat(Extension.resolveExtension("archive.tar.gz"))
              .isEqualTo(Extension.OTHER);
          softly
              .assertThat(Extension.resolveExtension("my.test.file.java"))
              .isEqualTo(Extension.JAVA);
        });
  }

  @Test
  @DisplayName("java 파일이면 true를 반환한다")
  void isJavaFile_true() {
    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(Extension.isJavaFile("Test.java")).isTrue();
          softly.assertThat(Extension.isJavaFile("Test.JAVA")).isFalse();
          softly.assertThat(Extension.isJavaFile("path/to/Test.java")).isTrue();
        });
  }

  @Test
  @DisplayName("java 파일이 아니면 false를 반환한다")
  void isJavaFile_false() {
    SoftAssertions.assertSoftly(
        softly -> {
          softly.assertThat(Extension.isJavaFile("config.yml")).isFalse();
          softly.assertThat(Extension.isJavaFile("script.kts")).isFalse();
          softly.assertThat(Extension.isJavaFile("README.md")).isFalse();
        });
  }

  @Test
  @DisplayName("확장자가 없는 파일은 false를 반환한다")
  void isJavaFile_noExtension() {
    assertThat(Extension.isJavaFile("README")).isFalse();
  }

  @DisplayName("클래스이름에 확장자를 추가합니다.")
  @Test
  void toJavaFileName() {
    // given
    String name = "com.test";

    // when & then
    assertThat(Extension.appendExtension(name, Extension.JAVA)).isEqualTo("com.test.java");
  }
}
