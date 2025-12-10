package com.opensourcereader.core.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.opensourcereader.core.analysis.exception.file.LocalDirectoryCreationException;
import com.opensourcereader.core.analysis.exception.file.LocalDirectoryDeletionException;
import com.opensourcereader.core.analysis.exception.file.RootDirectoryNotDeletableException;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitCloneDirectoryAlreadyExist;
import com.opensourcereader.core.analysis.exception.gitrepo.LocalGitCloneFileAlreadyExist;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtil {

  public static void createDirectory(File localPathFile) {
    validateFileExist(localPathFile);

    File parent = localPathFile.getParentFile();
    if (parent != null && !parent.exists()) {
      boolean created = parent.mkdirs();
      if (created) {
        return;
      }
      throw new LocalDirectoryCreationException().addDetail("parent", parent);
    }
  }

  public static void removeDirectory(String localPath) {
    Path projectDir = Paths.get("").toAbsolutePath().getParent().normalize();
    if (localPath.equals(projectDir.toString())) {
      throw new RootDirectoryNotDeletableException();
    }
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
    if (file.delete()) {
      return;
    }
    throw new LocalDirectoryDeletionException().addDetail("path", file.getAbsolutePath());
  }

  private static void validateFileExist(File localPathFile) {
    if (localPathFile.exists()) {
      if (localPathFile.isFile()) {
        throw new LocalGitCloneFileAlreadyExist();
      }
      throw new LocalGitCloneDirectoryAlreadyExist();
    }
  }
}
