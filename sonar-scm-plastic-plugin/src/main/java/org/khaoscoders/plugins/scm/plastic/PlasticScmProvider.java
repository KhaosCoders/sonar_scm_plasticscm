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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.scm.BlameCommand;
import org.sonar.api.batch.scm.ScmProvider;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PlasticScmProvider extends ScmProvider {

  private static final Logger LOG = Loggers.get(PlasticScmProvider.class);

  private final PlasticConfiguration configuration;
  private final PlasticBlameCommand blameCommand;

  public PlasticScmProvider(PlasticConfiguration configuration, PlasticBlameCommand blameCommand) {
    this.configuration = configuration;
    this.blameCommand = blameCommand;
  }

  @Override
  public String key() {
    return "PlasticSCM";
  }

  @Override
  public boolean supports(File baseDir) {
    File folder = baseDir;
    while (folder != null) {
      if (new File(folder, ".plastic").exists()) {
        return true;
      }
      folder = folder.getParentFile();
    }
    return false;
  }


  //@Override
  public BlameCommand blameCommand() {
    return blameCommand;
  }
}
