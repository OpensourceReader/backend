package com.opensourcereader.core.util;

import java.io.File;

public class FileUtil {

  private FileUtil() {
  }

  public static void createDirectory(String localPath) {
    File dir = new File(localPath);
    File parent = dir.getParentFile();

    if (parent != null && !parent.exists()) {
      boolean created = parent.mkdirs();
      if (!created) {
        throw new IllegalStateException("Failed to create parent directory: " + parent);
      }
    }
  }

  public static void removeDirectory(String localPath) {
    File dir = new File(localPath);
    deleteRecursively(dir);
  }

  private static void deleteRecursively(File file) {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          deleteRecursively(child);
        }
      }
    }
    if (!file.delete()) {
      throw new IllegalStateException("Failed to delete: " + file.getAbsolutePath());
    }
  }

}
