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

import org.sonar.api.Plugin;

import java.util.Collections;

public final class PlasticPlugin implements Plugin {

  @Override
  public void define(Context context) {
    context.addExtensions(PlasticScmProvider.class, PlasticConfiguration.class, PlasticBlameCommand.class);
    context.addExtensions(PlasticConfiguration.getProperties());
  }
}
