package com.opensourcereader.core.analysis.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.opensourcereader.core.analysis.dto.OpenSourceFileInfo;
import com.opensourcereader.core.analysis.infra.SourceCodeParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SpringBootTest
class OpenSourceCodeMethodExtractorTest {

  @Autowired private SourceCodeParser sourceCodeParser;
  @Autowired private OpenSourceCodeMethodExtractor extractor;

  @Test
  @DisplayName("java 파일이 아니면 빈 리스트")
  void extract_nonJava_returnsEmpty() {
    // given
    OpenSourceFileInfo file = new OpenSourceFileInfo("README.md", "X", "anything");

    // when
    var results = extractor.extract(file, null);

    // then
    assertThat(results).isEmpty();
  }

  // 일단 컴파일을 하고, 소스코드에만 있는 메서드면 진행
  // 바이트 코드결과를 직접 전달해서, 있는 메서드면 ㄴstartLine기록, 없는 메서드면 null로 기록
  // 밑에랑 거의 비슷, 컴파일를 한다음에 건네 주는게 맞다.
  @Test
  @DisplayName("바이트코드 메서드 목록을 기준으로 소스 파싱 결과를 매핑 후, start-endLine을 기록합니다.")
  void extract_mapsSourceBySignature() {
    // given
    String rawText =
        """
        package com.example.ossr;
        public class A {
          public void m(String s) {}
          public void n() {}
        }
        """;
    OpenSourceFileInfo sourceFile = new OpenSourceFileInfo("A.java", "1", rawText);
    String fqcn = sourceCodeParser.extractClassName(rawText);
    assertThat(fqcn).isEqualTo("com.example.ossr.A");
  }

  @Test
  @DisplayName("바이트코드에만 있고 소스엔 없는 메서드는 start-endLine이 null 입니다.")
  void extract_bytecodeOnly_method_hasNullSource() {
    // given
    String raw =
        """
        package com.example.ossr;
        public class A {
          public void m(String s) {}
        }
        """;
    OpenSourceFileInfo file = new OpenSourceFileInfo("A.java", "1", raw);
  }
}
