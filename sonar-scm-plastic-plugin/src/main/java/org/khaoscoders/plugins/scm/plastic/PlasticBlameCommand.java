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

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.scm.BlameCommand;
import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.command.Command;
import org.sonar.api.utils.command.CommandExecutor;
import org.sonar.api.utils.command.StreamConsumer;
import org.sonar.api.utils.command.StringStreamConsumer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class PlasticBlameCommand extends BlameCommand {

    private static final Logger LOG = Loggers.get(PlasticBlameCommand.class);
    private final CommandExecutor commandExecutor;
    private Settings settings;

    public PlasticBlameCommand(Settings settings) {
        this(CommandExecutor.create(), settings);
    }

    PlasticBlameCommand(CommandExecutor commandExecutor, Settings settings) {
        this.commandExecutor = commandExecutor;
        this.settings = settings;
    }

    @Override
    public void blame(BlameInput input, BlameOutput output) {
        FileSystem fs = input.fileSystem();
        LOG.debug("Working directory: " + fs.baseDir().getAbsolutePath());
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        List<Future<Void>> tasks = new ArrayList<>();
        for (InputFile inputFile : input.filesToBlame()) {
            tasks.add(submitTask(fs, output, executorService, inputFile));
        }

        for (Future<Void> task : tasks) {
            try {
                task.get();
            } catch (ExecutionException e) {
                // Unwrap ExecutionException
                throw e.getCause() instanceof RuntimeException ? (RuntimeException) e.getCause() : new IllegalStateException(e.getCause());
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private Future<Void> submitTask(final FileSystem fs, final BlameOutput result, ExecutorService executorService, final InputFile inputFile) {
        return executorService.submit(new Callable<Void>() {
            @Override
            public Void call() {
                blame(fs, inputFile, result);
                return null;
            }
        });
    }

    private void blame(FileSystem fs, InputFile inputFile, BlameOutput output) {
        String filename = inputFile.relativePath();
        //LOG.debug("BaseDir: " + fs.baseDir().getAbsolutePath());
        Command cl = createCommandLine(fs.baseDir(), filename);
        PlasticBlameConsumer consumer = new PlasticBlameConsumer(filename);
        StringStreamConsumer stderr = new StringStreamConsumer();

        int exitCode = execute(cl, consumer, stderr);
        if (exitCode != 0) {
            // Ignore the error since it may be caused by uncommited file
            LOG.debug("The PlasticSCM blame command [" + cl.toString() + "] failed: " + stderr.getOutput());
        }
        List<BlameLine> lines = consumer.getLines();

        // Fix PlasticSCM returns to few blame lines
        try {
            int linesOfCode = this.getLinesOfCode(filename);
            //LOG.debug("File: " + linesOfCode + " Plastic: " + lines.size() + " " + filename);
            if (lines.size() < linesOfCode) {
                Date date;
                if (lines.size() > 0) {
                    date = lines.get(lines.size() - 1).date();
                } else {
                    date = new Date(System.currentTimeMillis());
                }
                lines.add(new BlameLine().revision("0").author("?").date(date));
            }
        } catch(IOException ex) {
            // Do nothing
        }

        output.blameResult(inputFile, lines);
    }

    private int getLinesOfCode(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        int lines = 0;
        int chr = 0;
        while((chr = reader.read()) != -1) {
            if (lines == 0) {
                lines++;
            }
            if (chr == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private int execute(Command cl, StreamConsumer consumer, StreamConsumer stderr) {
        LOG.debug("Executing: " + cl);
        return commandExecutor.execute(cl, consumer, stderr, -1);
    }

    private Command createCommandLine(File workingDirectory, String filename) {
        String cm = settings.getString(PlasticConfiguration.CM_PROP_KEY);
        if (cm == null) {
            cm = "cm";
        }
        Command cl = Command.create(cm);
        cl.setDirectory(workingDirectory);
        cl.addArgument("blame");
        cl.addArgument(filename);
        // return dates always in this format
        cl.addArgument("--dateformat=\"" + PlasticBlameConsumer.CM_DATETIME_FORMAT + "\"");
        // return these infos
        cl.addArgument("--format=\"" + PlasticBlameConsumer.CM_BLAME_FORMAT + "\"");
        cl.addArgument("--encoding=utf-8");
        return cl;
    }
}
