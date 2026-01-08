package com.opensourcereader.core.board.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import com.opensourcereader.core.board.entity.PullCodeSnippet;
import com.opensourcereader.core.board.entity.PullRequestFile;
import com.opensourcereader.core.board.repository.PullCodeSnippetRepository;
import com.opensourcereader.core.board.repository.PullRequestFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PullRequestFileServiceTest {

  @Mock private PullRequestFileRepository pullRequestFileRepository;

  @Mock private PullCodeSnippetRepository pullCodeSnippetRepository;

  @InjectMocks private PullRequestFileService pullRequestFileService;

  @Test
  @DisplayName("정상적인 diff Map이 주어지면 파싱 후 데이터를 저장한다.")
  void success_save_test() {
    // given
    Integer tagNumber = 101;
    String mockDiff =
        """
        diff --git a/api/src/main/java/com/velocitypowered/api/proxy/config/ProxyConfig.java b/api/src/main/java/com/velocitypowered/api/proxy/config/ProxyConfig.java
        index 12d3dbd106..de9ea1b125 100644
        --- a/api/src/main/java/com/velocitypowered/api/proxy/config/ProxyConfig.java
        +++ b/api/src/main/java/com/velocitypowered/api/proxy/config/ProxyConfig.java
        @@ -83,9 +83,23 @@ public interface ProxyConfig {
        """;
    Map<Integer, String> diffMap = new HashMap<>();
    diffMap.put(tagNumber, mockDiff);
    // when
    boolean result = pullRequestFileService.save(diffMap);
    // then
    assertTrue(result);
    verify(pullRequestFileRepository, times(1)).save(any(PullRequestFile.class));
    verify(pullCodeSnippetRepository, times(1)).save(any(PullCodeSnippet.class));
  }

  @Test
  @DisplayName("diff 헤더 정보가 없는 경우에는 저장하지 않아야 한다.")
  void save_noHeader_DoNotSave() {
    // given
    Map<Integer, String> diffMap = new HashMap<>();
    diffMap.put(102, "Invalid diff content");

    // when
    boolean result = pullRequestFileService.save(diffMap);

    // then
    assertTrue(result);
    verify(pullRequestFileRepository, never()).save(any(PullRequestFile.class));
    verify(pullCodeSnippetRepository, never()).save(any(PullCodeSnippet.class));
  }
}
