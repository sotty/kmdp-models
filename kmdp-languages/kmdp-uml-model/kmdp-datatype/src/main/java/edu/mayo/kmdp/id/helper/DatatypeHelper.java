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
package edu.mayo.kmdp.id.helper;


import edu.mayo.kmdp.id.VersionedIdentifier;
import edu.mayo.kmdp.registry.Registry;
import edu.mayo.kmdp.util.URIUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.kmdp.util.adapters.DateAdapter;
import org.omg.spec.api4kp._1_0.identifiers.*;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.mayo.kmdp.util.Util.ensureUUIDFormat;

public class DatatypeHelper {

  private final static Pattern VERSIONS_RX = Pattern.compile("^(.*/)?(.*)/versions/(.+)$");
  private final static Pattern SEMVER_RX = Pattern.compile("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$");


  public static NamespaceIdentifier ns(final String nsUri) {
    return new NamespaceIdentifier().withId(URI.create(nsUri));
  }

  public static NamespaceIdentifier ns(final String nsUri, String label) {
    return new NamespaceIdentifier().withId(URI.create(nsUri)).withLabel(label);
  }

  public static NamespaceIdentifier ns(final String nsUri, String label, String version) {
    return new NamespaceIdentifier().withId(URI.create(nsUri))
        .withLabel(label)
        .withVersion(version);
  }


  public static ConceptIdentifier trm(final String termUri) {
    String uri = termUri;
    if (uri.matches("\\w+:.+")) {
      String candidatePfx = uri.substring(0, uri.indexOf(":"));
      String base = Registry.getNamespaceURIForPrefix(candidatePfx).orElse(null);
      if (base != null) {
        uri = base + "#" + termUri.substring(termUri.lastIndexOf(":") + 1);
      }
    }
    URI u = URI.create(uri);
    return new ConceptIdentifier()
        .withLabel(Util.isEmpty(u.getFragment())
            ? u.getPath().substring(u.getPath().lastIndexOf('/') + 1)
            : u.getFragment())
        .withRef(u);
  }

  public static ConceptIdentifier trm(final String termUri, final String label) {
    return trm(termUri).withLabel(label);
  }

  public static URIIdentifier uri(final String id, final String versionTag) {
    return new URIIdentifier()
        .withUri(URI.create(id))
        .withVersionId(
            Util.isEmpty(versionTag) ? null : URI.create(id + "/versions/" + versionTag));
  }


  public static URIIdentifier uri(String base, String id, String versionTag) {
    return uri(base + id, versionTag);
  }

  public static URIIdentifier vuri(final String uri, final String versionUri) {
    return
        new URIIdentifier()
            .withUri(URI.create(uri))
            .withVersionId(versionUri != null ? URI.create(versionUri) : null);
  }

  public static VersionedIdentifier deRef(Pointer ptr) {
    return toVersionIdentifier(ptr.getEntityRef());
  }

  public static URIIdentifier uri(final String id) {
    return uri(id, null);
  }

  public static VersionTagType tag(final String tag) {
    if (tag.matches("\\d+")) {
      return VersionTagType.SEQUENTIAL;
    }
    if (DateAdapter.isDate(tag)) {
      return VersionTagType.TIMESTAMP;
    }
    Matcher matcher = SEMVER_RX.matcher(tag);
    if (matcher.matches()) {
      return VersionTagType.SEM_VER;
    } else {
      return VersionTagType.GENERIC;
    }
  }

  public static QualifiedIdentifier name(final String n) {
    String pfx = n.substring(0, n.indexOf(":"));
    String name = n.substring(n.indexOf(':') + 1);
    String uri = Registry.getNamespaceURIForPrefix(pfx).orElse("");

    return new QualifiedIdentifier().withQName(new QName(uri, name, pfx));
  }

  public static String versionOf(URI versionedIdentifier, URI identifier) {
    if (versionedIdentifier == null) {
      return null;
    }
    // TODO Can probably be refactored to be more efficient...
    return toVersionIdentifier(versionedIdentifier).getVersion();
  }


  public static VersionIdentifier toVersionIdentifier(URIIdentifier uri) {
    return uri != null
        ? toVersionIdentifier(uri.getVersionId() != null ? uri.getVersionId() : uri.getUri())
        : null;
  }

  public static VersionIdentifier toVersionIdentifier(URI versionId) {
    return versionId != null ? toVersionIdentifier(versionId.toString()) : null;
  }

  public static VersionIdentifier toVersionIdentifier(String versionId) {
    URI uri = URI.create(versionId);
    String tag = uri.getFragment();
    String version = null;
    boolean hasTag = !Util.isEmpty(tag);

    uri = URIUtil.normalizeURI(uri);
    Matcher m = VERSIONS_RX.matcher(uri.toString());
    if (m.matches()) {
      version = m.group(3);
      if (!hasTag) {
        tag = m.group(2);
      }
    } else if (!hasTag) {
      tag = uri.toString();
      int index = tag.lastIndexOf('/');
      if (index >= 0) {
        tag = tag.substring(index + 1);
      } else {
        // Not sure what to do in this case, probably nothing...
      }
    }
    return new VersionIdentifier().withTag(tag).withVersion(version);
  }

  public static QualifiedIdentifier toQualifiedIdentifier(URIIdentifier id) {
    return new QualifiedIdentifier().withQName(URIUtil.toQName(id.getVersionId() != null
        ? id.getVersionId()
        : id.getUri())
        .orElseThrow(IllegalArgumentException::new));
  }

  public static QualifiedIdentifier toQualifiedIdentifier(ConceptIdentifier id) {
    return new QualifiedIdentifier().withQName(URIUtil.toQName(id.getRef())
        .orElseThrow(IllegalArgumentException::new));
  }

  public static QualifiedIdentifier toQualifiedIdentifier(URI id) {
    return new QualifiedIdentifier().withQName(URIUtil.toQName(id)
        .orElseThrow(IllegalArgumentException::new));
  }

  public static String toPrefixedName(QualifiedIdentifier qId) {
    return URIUtil.toPrefixedName(qId.getQName());
  }

  public static Optional<UUIDentifier> toUUIDentifier(ConceptIdentifier cid) {
    return ensureUUIDFormat(cid.getTag())
        .map((uuidStr) -> new UUIDentifier().withTag(uuidStr));
  }

  public static Optional<URIIdentifier> toURIIDentifier(UUIDentifier uid) {
    UUID uuid = uid.getUUID();
    if (uuid != null) {
      return Optional
          .ofNullable(new URIIdentifier().withUri(URI.create("uri:uuid:" + uuid.toString())));
    } else {
      return Optional.empty();
    }
  }

  public static String seedUUIDentifier(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes()).toString();
  }

  public static UUID seedUUID(String seed) {
    return UUID.nameUUIDFromBytes(seed.getBytes());
  }
}
