package io.xygeni.github.action.utils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * File utilities. To make the project self-contained.
 */
public class Files {

  private static final int DEFAULT_BUFFER_SIZE = 8192;

  /**
   * Unzip files
   * @param zipFile zip file
   * @param targetDir target directory
   * @throws IOException When there is a problem unzipping the file
   */
  public static void unzipFile(Path zipFile, Path targetDir) throws IOException {
    unzipFile(new ZipInputStream(openInputStream(zipFile.toFile())), targetDir);
  }

  private static void unzipFile(ZipInputStream zis, Path targetDir) throws IOException {
    try(zis) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        Path newPath = zipSlipProtect(zipEntry, targetDir);

        if (zipEntry.isDirectory()) {
          java.nio.file.Files.createDirectories(newPath);

        } else {
          if (newPath.getParent() != null) {
            if (java.nio.file.Files.notExists(newPath.getParent())) {
              java.nio.file.Files.createDirectories(newPath.getParent());
            }
          }
          java.nio.file.Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
        }
        zipEntry = zis.getNextEntry();
      }

      zis.closeEntry();
    }
  }

  private static BufferedInputStream openInputStream(File source) throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(source), DEFAULT_BUFFER_SIZE);
  }

  /**
   * To protect for <a href="https://github.com/snyk/zip-slip-vulnerability">Zip Slip vulnerability</a>.
   *
   * @param zipEntry Zip entry
   * @param targetDir Target directory
   * @return normalized path
   * @throws IOException if there is a bad entry (when the zip entry tries to "escape" target directory)
   */
  public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {
    Path targetDirResolved = targetDir.resolve(zipEntry.getName());
    Path normalizePath = targetDirResolved.normalize();
    if (!normalizePath.startsWith(targetDir)) {
      // This is intentional: Avoid writing locations out of targetDir
      throw new IOException("Bad zip entry: " + zipEntry.getName());
    }
    return normalizePath;
  }

}
