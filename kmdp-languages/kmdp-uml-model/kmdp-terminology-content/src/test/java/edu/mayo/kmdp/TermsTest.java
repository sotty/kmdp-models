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

import edu.mayo.kmdp.id.helper.DatatypeHelper;
import edu.mayo.kmdp.terms.TermsHelper;
import edu.mayo.kmdp.terms.krlanguage._2018._08.KRLanguage;
import org.junit.jupiter.api.Test;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.UUIDentifier;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TermsTest {

  @Test
  public void testEquality() {
    ConceptIdentifier c1 = TermsHelper.sct("f1", "x");
    ConceptIdentifier c2 = TermsHelper.sct("f1", "x");

    assertEquals(c1, c2);
    assertEquals(c1.getNamespace(), c2.getNamespace());
    assertNotSame(c1.getNamespace(), c2.getNamespace());
    assertEquals(c1.getNamespace().hashCode(), c2.getNamespace().hashCode());

  }


}
