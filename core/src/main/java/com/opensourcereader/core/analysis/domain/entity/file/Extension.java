package com.opensourcereader.core.analysis.domain.entity.file;

public enum Extension {
  JAVA,
  YML,
  KTS,
  XML,
  YAML,
  MD,
  BAT,
  ADOC,
  TXT,
  SQL,
  PNG,
  SVG,
  OTHER;

  public static Extension resolveExtension(String path) {
    int lastIndexOfZero = path.lastIndexOf('.') + 1;
    String pathExtension = path.substring(lastIndexOfZero);

    for (Extension extension : Extension.values()) {
      if (extension.name().toLowerCase().equals(pathExtension)) {
        return extension;
      }
    }
    return OTHER;
  }

  public static boolean isJavaFile(String path) {
    int lastIndexOfZero = path.lastIndexOf('.') + 1;
    return JAVA.name().toLowerCase().equals(path.substring(lastIndexOfZero));
  }

  public static String appendExtension(String className, Extension extension) {
    return className + "." + JAVA.name().toLowerCase();
  }
}
