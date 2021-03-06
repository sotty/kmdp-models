/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.mayo.kmdp.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

public class FileUtil {


  public static Optional<String> read(String file) {
    try {
      return read(new FileInputStream(file));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public static Optional<String> read(File file) {
    try {
      return read(new FileInputStream(file));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  public static Optional<String> read(InputStream is) {
    try {
      int available = is.available();
      byte[] data = new byte[available];
      int actual = is.read(data);

      if (available != actual) {
        throw new IOException("Unable to read all data");
      }

      return Optional.of(new String(data));
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }


  public static Optional<String> read(URL url) {
    try {
      File f = new File(url.toURI());
      if (!f.exists()) {
        throw new FileNotFoundException("Unable to find file for url " + url);
      }
      return read(f);
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }


  public static Optional<byte[]> readBytes(InputStream is) {
    try {
      DataInputStream dis = new DataInputStream(is);
      int available = is.available();
      byte[] data = new byte[available];
      dis.readFully(data);

      return Optional.of(data);
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static Optional<byte[]> readBytes(URL url) {
    try {
      return readBytes(url.openStream());
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public static List<URL> findFiles(URL rootURL, String regxpFilter) {
    try {
      File root = new File(rootURL.toURI());
      if (root.isDirectory()) {
        List<URL> files = new LinkedList<>();
        findFiles(root, regxpFilter, files);
        return files;
      } else if (root.getPath().matches(regxpFilter)) {
        return Collections.singletonList(root.toURI().toURL());
      } else {
        return Collections.emptyList();
      }
    } catch (URISyntaxException | MalformedURLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private static void findFiles(File root, String regxpFilter, List<URL> fileURLs)
      throws MalformedURLException {
    if (root == null) {
      return;
    }
    for (File child : Objects.requireNonNull(root.listFiles())) {
      if (child.isDirectory()) {
        findFiles(child, regxpFilter, fileURLs);
      } else {
        if (child.getPath().matches(regxpFilter)) {
          fileURLs.add(child.toURI().toURL());
        }
      }
    }
  }

  public static boolean copyTo(URL source, File targetFile) {
    try {
      return copyTo(source.openStream(), targetFile);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean copyTo(InputStream sourceStream, File targetFile) {
    try {
      if (targetFile.isDirectory()) {
        return false;
      }

      BufferedReader reader = new BufferedReader(new InputStreamReader(sourceStream));
      StringBuilder out = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
      reader.close();

      if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
        return false;
      }

      FileOutputStream fos = new FileOutputStream(targetFile);
      fos.write(out.toString().getBytes());
      fos.close();

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static void delete(File root) {
    Path pathToBeDeleted = root.toPath();

    try {
      Files.walk(pathToBeDeleted)
          .sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static File relativePathToFile(String rootPath, String relativePath) {
    return new File(rootPath + asPlatformSpecific(relativePath));
  }


  public static String asPlatformSpecific(String path) {
    return path.contains("/")
        ? path.replaceAll("/", Matcher.quoteReplacement(File.separator))
        : path;
  }

  public static void write(byte[] content, File file) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      fos.write(content);
      fos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
  public static void write(String content, File file) {
    PrintWriter wr = null;
    try {
      wr = new PrintWriter(file);
      wr.write(content);
      wr.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      if (wr != null) {
        wr.close();
      }
    }
  }
}
