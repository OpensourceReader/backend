package com.opensourcereader.core.util;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegexPatternsUtil {

  public static final Pattern DIFF_HEADER = Pattern.compile("^diff --git a/(.*) b/(.*)$");
  public static final Pattern DIFF_SUMMARY = Pattern.compile("^@@ -(\\d*),?(\\d*) \\+(\\d*),?(\\d*) @@ (.*)$");
}
