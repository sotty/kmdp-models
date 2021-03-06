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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.validation.constraints.NotNull;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.jvnet.mjiip.v_2_2.XJC22Mojo;

public abstract class CodeGenTestBase {

  public static List<Diagnostic<? extends JavaFileObject>> doCompile(File source, File gen,
      File target) {
    List<File> list = new LinkedList<File>();

    explore(source, list);
    if (gen != source) {
      explore(gen, list);
    }

    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    StandardJavaFileManager fileManager = jc.getStandardFileManager(diagnostics, null, null);
    Iterable<? extends JavaFileObject> compilationUnits =
        fileManager.getJavaFileObjectsFromFiles(list);
    List<String> jcOpts = Arrays.asList("-d", target.getPath());
    JavaCompiler.CompilationTask task = jc
        .getTask(null, fileManager, diagnostics, jcOpts, null, compilationUnits);
    task.call();
    return diagnostics.getDiagnostics();
  }

  public static void ensureSuccessCompile(File src, File gen, File target) {
    List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile(src, gen, target);

    boolean success = true;
    for (Diagnostic diag : diagnostics) {
//      System.out.println(diag.getKind() + " : " + diag);
      if (diag.getKind() == Diagnostic.Kind.ERROR) {
        success = false;
      }
    }
    assertTrue(success);
  }


  public static void showDirContent(File folder) {
    showDirContent(folder, 0);
  }

  public static void showDirContent(File file, int i) {
//    System.out.println(tab(i) + " " + file.getName());
    if (file.isDirectory()) {
      for (File sub : file.listFiles()) {
        showDirContent(sub, i + 1);
      }
    }
  }

  public static String tab(int n) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < n; j++) {
      sb.append("\t");
    }
    return sb.toString();
  }

  public static void explore(File dir, List<File> files) {
    for (File f : dir.listFiles()) {
      if (f.getName().endsWith(".java")) {
        files.add(f);
      }
      if (f.isDirectory()) {
        explore(f, files);
      }
    }
  }


  public static Class<?> getNamedClass(String name, File tgt) {
    try {
      ClassLoader urlKL = new URLClassLoader(
          new URL[]{tgt.toURI().toURL()},
          Thread.currentThread().getContextClassLoader()
      );

      return Class.forName(name, true, urlKL);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    return Object.class;
  }

  public static String deployResource(String resourcePath, File targetFolder, String targetFileName) {
    return deployResource(resourcePath, targetFolder, targetFileName, CodeGenTestBase::read);
  }

  public static byte[] read(InputStream inputStream) {
    Optional<String> content = FileUtil.read(inputStream);
    if (!content.isPresent()) {
      fail("Unable to read file content ");
      return new byte[0];
    } else {
      return content.get().getBytes();
    }
  }


  public static String deployResource(String resourcePath, File targetFolder, String targetFileName,
      Function<InputStream, byte[]> mapper) {
    return deployResource(CodeGenTestBase.class.getResourceAsStream(resourcePath), targetFolder,
        targetFileName, mapper);
  }

  public static String deployResource(InputStream is, File targetFolder, String targetFileName,
      Function<InputStream, byte[]> mapper) {
    assertTrue(targetFolder.exists());
    assertTrue(targetFolder.isDirectory());
    File out = new File(targetFolder.getAbsolutePath() + File.separator + targetFileName);
    FileUtil.write(mapper.apply(is),out);
    return out.getAbsolutePath();
  }


  public static void printSourceFile(File f, PrintStream out) {
    try {
      FileInputStream inputStream = new FileInputStream(f);
      int n = inputStream.available();
      byte[] buf = new byte[n];
      if (n == inputStream.read(buf)) {
        out.println(new String(buf));
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen) {
    applyJaxb(schemas, binds, gen, null);
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen, File catalog) {
    applyJaxb(schemas, binds, gen, null, catalog, false, false);
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen,
      boolean withAnnotations) {
    applyJaxb(schemas, binds, gen, null, null, withAnnotations, false);
  }

  public static void applyJaxb(List<File> schemas, List<File> binds, File gen, File episode,
      File catalog, boolean withAnnotations, boolean withExtensions) {
    schemas.forEach((src) -> {
      if (!src.exists()) {
        fail("Schema File or Dir Not Found : " + src);
      }
    });
    binds.forEach((xjb) -> {
      if (!xjb.exists()) {
        fail("Schema File or Dir Not Found : " + xjb);
      }
    });
    if (!gen.exists() || !gen.isDirectory()) {
      fail("Generated Source Dir Not Found : " + gen);
    }

    Options opts = new Options();
    opts.targetDir = gen;

    schemas.forEach((src) -> {
      if (src.isDirectory()) {
        opts.addGrammarRecursive(src);
      } else {
        opts.addGrammar(src);
      }
    });

    binds.forEach((xjb) -> {
      if (xjb.isDirectory()) {
        opts.addBindFileRecursive(xjb);
      } else {
        opts.addBindFile(xjb);
      }
    });

    opts.compatibilityMode = Options.EXTENSION;

    if (catalog != null) {
      if (!catalog.exists()) {
        fail("Catalog File Not Found : " + catalog);
      }
      try {
        opts.addCatalog(catalog);
      } catch (IOException e) {
        e.printStackTrace();
        fail(e.getMessage());
      }
    }

    XJC22Mojo mojo = new XJC22Mojo();
    //mojo.setLog(new SystemStreamLog());
    mojo.setVerbose(false);
    mojo.setExtension(true);

    mojo.setEpisode(true);
    mojo.setAddIfExistsToEpisodeSchemaBindings(true);
    mojo.setScanDependenciesForBindings(true);
    mojo.setUseDependenciesAsEpisodes(true);
    mojo.setDebug(false);

    if (episode != null) {
      mojo.setEpisodeFile(episode);
    }

    if (withExtensions) {
      mojo.getArgs().add("-Xinheritance");
      mojo.getArgs().add("-Xnamespace-prefix");
      mojo.getArgs().add("-Xfluent-api");
    }

    if (withAnnotations) {
      mojo.getArgs().add("-Xannotate");
      opts.pluginURIs.add("http://annox.dev.java.net");
    }

    try {
      opts.parseArguments(mojo.getArgs().toArray(new String[mojo.getArgs().size()]));
      mojo.doExecute(opts);
    } catch (MojoExecutionException | BadCommandLineException e) {
      e.printStackTrace();
    }
  }


  public static void deploy(InputStream is, File root, String path) {
    assertTrue(FileUtil.copyTo(is, FileUtil.relativePathToFile(root.getAbsolutePath(), path)));
  }

  public static void deploy(File root, String path) {
    URL def = CodeGenTestBase.class.getResource(path);
    assertTrue(FileUtil.copyTo(def, FileUtil.relativePathToFile(root.getAbsolutePath(), path)));
  }

  public static void deploy(File root, String path, @NotNull Class<?> context) {
    URL def = context.getResource(path);
    assertTrue(FileUtil.copyTo(def, FileUtil.relativePathToFile(root.getAbsolutePath(), path)));
  }

  public static File initSourceFolder(File folder) {
    return initFolder(folder,"test");
  }
  public static File initTargetFolder(File folder) {
    return initFolder(folder,"out");
  }

  public static File initGenSourceFolder(File folder) {
    return initFolder(folder,"gen");
  }

  public static File initFolder(File folder, String subFolder) {
    if ( ! folder.exists()) {
      return null;
    }
    File f = new File(folder,subFolder);
    if (!f.exists()) {
      if (!f.mkdirs()) {
        return null;
      }
    }
    return f;
  }

}
