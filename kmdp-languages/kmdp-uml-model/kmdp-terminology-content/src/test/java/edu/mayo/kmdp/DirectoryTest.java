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

import edu.mayo.kmdp.terms.KMDPTerms;
import edu.mayo.kmdp.terms.kao.knowledgeassettype._1_0.KnowledgeAssetType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectoryTest {

  @Test
  public void testDirectoryPopulation() {
    assertEquals(KnowledgeAssetType.Formal_Ontology,
        KMDPTerms.directory.resolveRef(KnowledgeAssetType.Formal_Ontology.getRef().toString(),
            KnowledgeAssetType.class).get());
  }
}
