package com.opensourcereader.core.board.service;

import static com.opensourcereader.core.util.RegexPatternsUtil.DIFF_HEADER;
import static com.opensourcereader.core.util.RegexPatternsUtil.DIFF_SUMMARY;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opensourcereader.core.board.entity.PullCodeSnippet;
import com.opensourcereader.core.board.entity.PullRequestFile;
import com.opensourcereader.core.board.repository.PullCodeSnippetRepository;
import com.opensourcereader.core.board.repository.PullRequestFileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PullRequestFileService {

  private final PullRequestFileRepository pullRequestFileRepository;
  private final PullCodeSnippetRepository pullCodeSnippetRepository;

  @Transactional
  public boolean save(Map<Integer, String> diffMap) {
    Set<Integer> tagNumbers = diffMap.keySet();
    for (Integer tagNumber : tagNumbers) {
      String classPath = null;
      int startLine = 0;
      int endLine = 0;
      try (BufferedReader reader = new BufferedReader(new StringReader(diffMap.get(tagNumber)))) {
        String line;
        while ((line = reader.readLine()) != null) {
          Matcher headLine = DIFF_HEADER.matcher(line);
          if (headLine.matches()) {
            classPath = headLine.group(2);
            continue;
          }
          Matcher diff_summary = DIFF_SUMMARY.matcher(line);
          if (diff_summary.matches()) {
            startLine =
                Math.min(
                    Integer.parseInt(diff_summary.group(1)),
                    Integer.parseInt(diff_summary.group(3)));
            endLine =
                Math.max(
                    Integer.parseInt(diff_summary.group(2)),
                    Integer.parseInt(diff_summary.group(4)));
          }
        }
        if (classPath != null) {
          PullRequestFile pullRequestFile = PullRequestFile.of(tagNumber, classPath);
          PullCodeSnippet pullCodeSnippet = PullCodeSnippet.of(pullRequestFile, startLine, endLine);

          pullRequestFileRepository.save(pullRequestFile);
          pullCodeSnippetRepository.save(pullCodeSnippet);
        }
      } catch (Exception e) {
        return false;
      }
    }
    return true;
  }
}
