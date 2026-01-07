package com.opensourcereader.core.board.service;


import static com.opensourcereader.core.util.RegexPatternsUtil.DIFF_HEADER;
import static com.opensourcereader.core.util.RegexPatternsUtil.DIFF_SUMMARY;

import com.opensourcereader.core.board.entity.PullCode;
import com.opensourcereader.core.board.entity.PullCodeLine;
import com.opensourcereader.core.board.repository.PullCodeLineRepository;
import com.opensourcereader.core.board.repository.PullCodeRepository;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PullCodeService {

  private final PullCodeRepository pullCodeRepository;
  private final PullCodeLineRepository pullCodeLineRepository;

  public boolean save(Map<Integer, String> diffMap) {
    Set<Integer> tagNumbers = diffMap.keySet();
    for (Integer tagNumber : tagNumbers) {
      String classPath = null;
      int startLine = 0;
      int endLine = 0;
      try(BufferedReader reader = new BufferedReader(new StringReader(diffMap.get(tagNumber)))){
        String line;
        while ((line = reader.readLine()) != null) {
          Matcher headLine = DIFF_HEADER.matcher(line);
          if (headLine.matches()) {
            classPath = headLine.group(2);
            continue;
          }
          Matcher diff_summary = DIFF_SUMMARY.matcher(line);
          if (diff_summary.matches()) {
            startLine = Math.min(Integer.parseInt(diff_summary.group(1)),
                Integer.parseInt(diff_summary.group(3)));
            endLine = Math.max(Integer.parseInt(diff_summary.group(2)),
                Integer.parseInt(diff_summary.group(4)));
          }
        }
        if (classPath != null) {
          PullCode pullCode = PullCode.of(tagNumber, classPath);
          PullCodeLine pullCodeLine = PullCodeLine.of(pullCode, startLine, endLine);

          pullCodeRepository.save(pullCode);
          pullCodeLineRepository.save(pullCodeLine);
        }
      }catch(Exception e){

      }
    }
    return true;
  }
}
