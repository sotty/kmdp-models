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
package edu.mayo.kmdp.terms.generator;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import edu.mayo.kmdp.terms.generator.config.EnumGenerationConfig;

import java.io.File;
import java.util.Map;

public class JavaEnumTermsGenerator extends BaseEnumGenerator {

  public void generate(SkosTerminologyAbstractor.ConceptGraph conceptGraph,
      File outputDir) {
    this.generate(conceptGraph, new EnumGenerationConfig(), outputDir);
  }

  public void generate(SkosTerminologyAbstractor.ConceptGraph conceptGraph,
      EnumGenerationConfig options,
      File outputDir) {
    outputDir.mkdirs();

    this.generateConcepts(conceptGraph, options, outputDir);
  }

  protected void generateConcepts(SkosTerminologyAbstractor.ConceptGraph conceptGraph,
      EnumGenerationConfig options,
      File outputDir) {

    for (ConceptScheme<Term> conceptScheme : conceptGraph.getConceptSchemes()) {

      Map<String, Object> context = getContext(conceptScheme, options, conceptGraph);

      String mainText = fromTemplate("concepts-java", context);

      this.writeToFile(mainText,
          getFile(outputDir, context, ".java"));
    }

  }


}
