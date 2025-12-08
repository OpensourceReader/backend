package com.opensourcereader.core.util;


import com.opensourcereader.core.analysis.exception.file.LocalDirectoryCreationException;
import com.opensourcereader.core.analysis.exception.file.LocalDirectoryDeletionException;
import java.io.File;

public class FileUtil {

  private FileUtil() {
  }

  public static void createDirectory(File localPathFile) {
    File parent = localPathFile.getParentFile();

    if (parent != null && !parent.exists()) {
      boolean created = parent.mkdirs();
      if (!created) {
        throw new LocalDirectoryCreationException()
            .addDetail("parent", parent);
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
      throw new LocalDirectoryDeletionException()
          .addDetail("path", file.getAbsolutePath());
    }
  }

}
