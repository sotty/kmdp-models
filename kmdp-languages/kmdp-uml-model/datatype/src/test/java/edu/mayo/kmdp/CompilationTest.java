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
package edu.mayo.kmdp;

import edu.mayo.kmdp.util.JaxbUtil;
import edu.mayo.kmdp.util.XMLUtil;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import org.omg.spec.api4kp._1_0.identifiers.Pointer;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import javax.xml.validation.Schema;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import static edu.mayo.kmdp.id.helper.DatatypeHelper.uri;
import static edu.mayo.kmdp.util.CodeGenTestBase.*;
import static edu.mayo.kmdp.util.XMLUtil.catalogResolver;
import static edu.mayo.kmdp.util.XMLUtil.getSchemas;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@EnableRuleMigrationSupport
public class CompilationTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();


  @Test
  public void testJaxbGeneration() {
    File tgt = compile();

    Class<?> ptrClass = getNamedClass("edu.mayo.kmdp.common.model.Pointer", tgt);
    assertNotNull(ptrClass);
    assertTrue(Pointer.class.isAssignableFrom(ptrClass));

    try {

      Object ptr1 = ptrClass.newInstance();
      URI foo = URI.create("http://mock");
      ptrClass.getMethod("setName", String.class).invoke(ptr1, "Test");
      ptrClass.getMethod("setType", URI.class).invoke(ptr1, foo);
      ptrClass.getMethod("setHref", URI.class).invoke(ptr1, foo);
      ptrClass.getMethod("setEntityRef", URIIdentifier.class).invoke(ptr1, uri(foo.toString()));

      String xml = JaxbUtil.marshallToString(Collections.singleton(Pointer.class),
          ptr1,
          JaxbUtil.defaultProperties());
      System.out.println(xml);

      Optional<Schema> schema = getSchemas(
          DatatypeTest.class.getResource("/xsd/API4KP/api4kp/identifiers/identifiers.openapi.xsd"),
          catalogResolver("/xsd/api4kp-catalog.xml"));
      assertTrue(schema.isPresent());
      XMLUtil.validate(xml, schema.get());

      Optional<Pointer> asPtr = JaxbUtil
          .unmarshall(ptrClass, Pointer.class, xml, JaxbUtil.defaultProperties());
      assertTrue(asPtr.isPresent());
      Object p2 = asPtr.get();

      assertEquals("Test", ((Pointer) p2).getName());
      assertNotNull(((Pointer) p2).getEntityRef());
      assertNotNull(((Pointer) p2).getType());

      assertTrue(Pointer.class.isInstance(p2));

      Pointer p3 = new Pointer();
      ((Pointer) p2).copyTo(p3);

      assertEquals(((Pointer) p2).getType(), p3.getType());

      Object x = ptrClass.newInstance();

      p3.copyTo(x);
      assertEquals(((Pointer) x).getType(), ((Pointer) ptr1).getType());


    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  private File compile() {
    folder.getRoot().mkdirs();
    assertTrue(folder.getRoot().exists());

    File src = initSourceFolder(folder);
    File gen = initGenSourceFolder(folder);
    File tgt = initTargetFolder(folder);

    deploy(src, "/xsd/API4KP/api4kp/identifiers/identifiers.openapi.xsd", CompilationTest.class);
    deploy(src, "/xsd/API4KP/api4kp/identifiers/identifiers.xsd", CompilationTest.class);

    showDirContent(folder);

    applyJaxb(Collections.singletonList(src), Collections.emptyList(), gen, true);

    printSourceFile(new File(gen.getAbsolutePath() + "/edu/mayo/kmdp/common/model/Pointer.java"),
        System.out);

    ensureSuccessCompile(src, gen, tgt);

    return tgt;
  }


}
