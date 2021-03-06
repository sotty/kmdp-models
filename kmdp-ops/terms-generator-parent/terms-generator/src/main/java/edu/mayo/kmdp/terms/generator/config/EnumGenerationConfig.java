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
package edu.mayo.kmdp.terms.generator.config;

import edu.mayo.kmdp.ConfigProperties;
import edu.mayo.kmdp.Opt;
import edu.mayo.kmdp.Option;
import java.util.Properties;

public class EnumGenerationConfig extends
    ConfigProperties<EnumGenerationConfig, EnumGenerationConfig.EnumGenerationParams> {

  private static final Properties defaults = defaulted(EnumGenerationParams.class);

  public EnumGenerationConfig() {
    super(defaults);
  }

  @Override
  public EnumGenerationParams[] properties() {
    return EnumGenerationParams.values();
  }

  public enum EnumGenerationParams implements Option<EnumGenerationParams> {

    PACKAGE_NAME(Opt.of(
        "packageName",
        "",
        "Name of the Java package",
        String.class,
        true)),
    WITH_JAXB(Opt.of(
        "withJaxb",
        "false",
        "Enable Jaxb support",
        Boolean.class,
        false)),
    WITH_JSONLD(Opt.of(
        "withJsonLD",
        "false",
        "Enable JSON-LD support",
        Boolean.class,
        false)),
    WITH_JSON(Opt.of(
        "withJson",
        "false",
        "Enable JSON support",
        Boolean.class,
        false)),
    TERMS_PROVIDER(Opt.of(
        "termsProvider",
        "",
        "Java Terminology registry to register the Enumeration",
        String.class,
        false));

    private Opt opt;

    EnumGenerationParams(Opt opt) {
      this.opt = opt;
    }

    @Override
    public Opt getOption() {
      return opt;
    }

  }
}