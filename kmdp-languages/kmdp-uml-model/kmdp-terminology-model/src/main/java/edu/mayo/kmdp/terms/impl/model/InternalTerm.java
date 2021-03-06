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
package edu.mayo.kmdp.terms.impl.model;

import edu.mayo.kmdp.id.Term;
import edu.mayo.kmdp.terms.ConceptScheme;
import org.omg.spec.api4kp._1_0.identifiers.ConceptIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.NamespaceIdentifier;
import org.omg.spec.api4kp._1_0.identifiers.URIIdentifier;

import java.net.URI;

public class InternalTerm extends ConceptIdentifier {

  private ConceptScheme<Term> scheme;
  private String tag;
  private String comment;

  public InternalTerm(URI conceptURI, String code, String label, String comment, URI refUri,
      ConceptScheme<Term> scheme) {
    this.setRef(refUri);
    this.setTag(code);
    this.setLabel(label);
    this.setComment(comment);
    this.scheme = scheme;
    this.conceptId = conceptURI;
    if (scheme != null) {
      this.namespace = new NamespaceIdentifier()
          .withId(scheme.getId())
          .withVersion(scheme.getVersion());
    }
  }

  public ConceptScheme<Term> getScheme() {
    return scheme;
  }

  @Override
  public boolean equals(Object object) {
    boolean e = super.equals(object);
    return e;
  }

  @Override
  public int hashCode() {
    int x = super.hashCode();
    return x;
  }

  public String toString() {
    return getRef().toString();
  }

  @Override
  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
