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

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.XMLUtil;
import edu.mayo.kmdp.xslt.XSLTConfig;
import edu.mayo.kmdp.xslt.XSLTConfig.XSLTOptions;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.Swagger;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import static edu.mayo.kmdp.util.SwaggerTestUtil.parseValidate;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class XSDtoYAMLTranslatorTest {

  @Test
  public void testComplexXSLT() throws IOException {
    String source = "/xsd/metadata/surrogate/surrogate.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL catalog = XSDtoYAMLTranslatorTest.class.getResource("/catalog-yaml.xml");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String base = sourceUrl.toString().substring(0, sourceUrl.toString().lastIndexOf("/") + 1);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl,
        xslt,
        new XSLTConfig()
            .with(XSLTOptions.CATALOGS, catalog.toString())));
    //System.out.println(out);

    Swagger model = parseValidate(out);

  }


  @Test
  public void testWithXSDDynamicallyResolved() throws URISyntaxException, IOException {
    String source = "/xsd/withDynamicEnum.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL catalog = XSDtoYAMLTranslatorTest.class.getResource("/catalog-yaml.xml");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String base = sourceUrl.toString().substring(0, sourceUrl.toString().lastIndexOf("/") + 1);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl,
        xslt,
        new XSLTConfig()
            .with(XSLTOptions.CATALOGS, catalog.toString())));
    //System.out.println(out);

    Swagger model = parseValidate(out);
    assertNotNull(model.getDefinitions());
  }

  @Test
  public void testWithXSDImport() {
    String source = "/xsd/mock/identifiers/identifiers.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl,
        xslt,
        new XSLTConfig()));
    //System.out.println(out);

    Swagger model = parseValidate(out);
    assertNotNull(model.getDefinitions());
  }

  @Test
  public void testWithEnum() {
    String source = "/xsd/withEnum.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl, xslt, new XSLTConfig()));
    //System.out.println(out);

    Swagger model = parseValidate(out);
    assertNotNull(model.getDefinitions());

    Model en = model.getDefinitions().get("KnowledgeAssetCategory");
    assertNotNull(en);
    assertNotNull(((ModelImpl) en).getEnum());
    assertEquals(8, ((ModelImpl) en).getEnum().size());

  }


  @Test
  public void testComplexWithTypes() {
    String source = "/xsd/complex.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl, xslt, new XSLTConfig()));

//		System.out.println( out );
    Swagger model = parseValidate(out);

    Model opDef = model.getDefinitions().get("OperationalDefinition");
    assertNotNull(opDef);

    assertTrue(opDef instanceof ComposedModel);
    Model core = ((ComposedModel) opDef).getAllOf().get(1);
    assertNotNull(core);
    assertNotNull(core.getProperties());
    assertTrue(core.getProperties().containsKey("aaS"));

    Property aas = core.getProperties().get("aaS");
    assertTrue(aas instanceof RefProperty);
    assertNotNull(aas.getXml());
    assertEquals("http://kmdp.mock.edu/ccg/model", aas.getXml().getNamespace());
  }


  @Test
  public void testSimpleWithAttributes() {
    String source = "/xsd/mock/identifiers/tags/tags.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl, xslt, new XSLTConfig()));

    //System.out.println(out);
    Swagger model = parseValidate(out);

    Model tag = model.getDefinitions().get("SemVerTag");
    assertNotNull(tag);

    assertTrue(tag instanceof ComposedModel);

    ComposedModel ctag = (ComposedModel) tag;
    assertEquals(2, ctag.getAllOf().size());

    tag = ctag.getAllOf().get(0);
    assertTrue(tag instanceof RefModel);
    assertTrue(tag.getReference().contains("VersionTag"));

    tag = ctag.getAllOf().get(1);
    assertTrue(tag.getProperties().containsKey("major"));

    Property minor = tag.getProperties().get("minor");
    assertNotNull(minor);
    assertTrue(minor.getXml() != null);
    assertTrue(minor.getXml().getAttribute());
    assertEquals("string", minor.getType());
  }

  private String wrap(String out) {
    return out;
  }


  @Test
  public void testParserValidator() {
    Optional<String> data = FileUtil.read(XSDtoYAMLTranslatorTest.class.getResource("/test.yaml"));

    if (!data.isPresent()) {
      fail("Unable to open sample data");
    }

    Swagger model = parseValidate(data.get());
    Model pet = model.getDefinitions().get("Test");
    assertEquals(Stream.of("id", "name", "tag", "vals").collect(toSet()),
        pet.getProperties().keySet());
  }


  @Test
  public void testWithExternalizedGroups() {
    String source = "/xsd/withExternalizedContentGroups.xsd";

    URL xslt = XSDtoYAMLTranslatorTest.class.getResource("/edu/mayo/kmdp/xsd/xsd-to-yamls.xsl");
    URL sourceUrl = XSDtoYAMLTranslatorTest.class.getResource(source);

    String out = wrap(XMLUtil.applyXSLTSimple(sourceUrl,
        xslt,
        new XSLTConfig()));
    //System.out.println(out);

    Swagger model = parseValidate(out);
    assertNotNull(model.getDefinitions());

    Model td = model.getDefinitions().get("TaskDecision");
    assertNotNull(td);
    assertTrue(td.getProperties().containsKey("know"));

    Model kh = model.getDefinitions().get("Knowledge");
    assertNotNull(kh);
    assertTrue(kh.getProperties().containsKey("label"));
    assertTrue(kh.getProperties().get("label").getRequired());

    assertTrue(kh.getProperties().containsKey("foo"));

    Property foo = kh.getProperties().get("foo");
    assertEquals("array", foo.getType());
    assertTrue(foo.getRequired());
    assertTrue(foo instanceof ArrayProperty);
    ArrayProperty afoo = (ArrayProperty) foo;
    assertEquals(3, (int) afoo.getMinItems());
    assertEquals(5, (int) afoo.getMaxItems());

    Model e1 = model.getDefinitions().get("Enum1");
    assertNotNull(e1);
    assertEquals(2, ((ModelImpl) e1).getEnum().size());

  }


}
