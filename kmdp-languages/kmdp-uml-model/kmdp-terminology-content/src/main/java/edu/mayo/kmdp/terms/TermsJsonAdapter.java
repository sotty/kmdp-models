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
package edu.mayo.kmdp.terms;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import edu.mayo.kmdp.id.Term;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;

import java.io.IOException;


public abstract class TermsJsonAdapter {

  public static class Serializer extends JsonSerializer<Term> {

    @Override
    public void serialize(Term v, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(v.asConcept());
    }
  }

  public static class Deserializer extends JsonDeserializer<Term> {

    @Override
    public Term deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      ConceptIdentifier cid = jp.readValueAs(ConceptIdentifier.class);
      return KMDPTerms.directory.resolve(cid.getNamespace().getId(), cid.getTag()).orElse(null);
    }
  }
}
