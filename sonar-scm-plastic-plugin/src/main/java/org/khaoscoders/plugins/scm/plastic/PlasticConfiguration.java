/*
 * SonarQube :: Plugins :: SCM :: PlasticSCM
 * Copyright (C) 2021 Khaos-Coders
 * mailto:info AT khaos-coders DOT org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.khaoscoders.plugins.scm.plastic;

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.scanner.ScannerSide;

@ScannerSide
public class PlasticConfiguration {

    private static final String CATEGORY_PLASTIC = "PlasticSCM";
    public static final String CM_PROP_KEY = "kc.plastic.cm";

    private final Configuration config;

    public PlasticConfiguration(Configuration config) {
        this.config = config;
    }

    public static List<PropertyDefinition> getProperties() {
        return Arrays.asList(
                PropertyDefinition.builder(CM_PROP_KEY)
                        .name("CM Path")
                        .description("Path to cm cli executable")
                        .type(PropertyType.STRING)
                        .onQualifiers(Qualifiers.APP)
                        .category(CoreProperties.CATEGORY_SCM)
                        .subCategory(CATEGORY_PLASTIC)
                        .index(0)
                        .build()
        );
    }

    @CheckForNull
    public String cm() {
        return config.get(CM_PROP_KEY).orElse(null);
    }
}
