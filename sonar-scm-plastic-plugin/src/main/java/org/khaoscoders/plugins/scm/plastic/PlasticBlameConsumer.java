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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.utils.command.StreamConsumer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PlasticBlameConsumer implements StreamConsumer {

    private static final Logger LOG = Loggers.get(PlasticBlameConsumer.class);

    public static final String CM_DATETIME_FORMAT = "dd.MM.yyyy-HH:mm:ss";

    public static final String CM_BLAME_FORMAT = "'{owner}','{date}',{changeset},{line},'{rev}','{branch}'";
    private static final String CM_BLAME_PATTERN = "^'([^\\']*)','(\\d\\d\\.\\d\\d\\.\\d{4}-\\d\\d:\\d\\d:\\d\\d)',(-?\\d+),(\\d+),'([^\\']*)','([^\\']*)'";

    private List<BlameLine> lines = new ArrayList<>();

    private DateFormat format;

    private final String filename;

    private Pattern pattern;

    public PlasticBlameConsumer(String filename) {
        this.filename = filename;
        format = new SimpleDateFormat(CM_DATETIME_FORMAT, Locale.ENGLISH);
        pattern = Pattern.compile(CM_BLAME_PATTERN);
    }

    @Override
    public void consumeLine(String line) {
        String trimmedLine = line.trim();
        //LOG.debug("Consume line: " + line);
        /* '<owner>' '09.18.2017 09:23:19' <changeset> <line> '<rev>' '<branch>' */
        /* '<owner>' '01.21.2021 21:03:48' -1 84 'local' '' */

        Matcher matcher = pattern.matcher(trimmedLine);
        if (!matcher.matches()) {
            throw new IllegalStateException("Unable to blame file " + filename + ". Unrecognized blame info at line " + (getLines().size() + 1) + ": " + trimmedLine);
        }
        String author = matcher.group(1);

        String revision = matcher.group(5);
        String dateStr = matcher.group(2);

        //LOG.debug("Author: " + author + " Revision: " + revision + " Date: " + dateStr);
        Date dateTime = null;
        try {
            dateTime = format.parse(dateStr);
        } catch (ParseException e) {
            LOG.warn("skip ParseException on file " + filename + " at line " + (getLines().size() + 1) + ": " + e.getMessage() + " during parsing date " + dateStr
                            + " with pattern " + CM_DATETIME_FORMAT + " [" + line + "]", e);
        }

        lines.add(new BlameLine().date(dateTime).revision(revision).author(author));
    }

    public List<BlameLine> getLines() {
        return lines;
    }
}
