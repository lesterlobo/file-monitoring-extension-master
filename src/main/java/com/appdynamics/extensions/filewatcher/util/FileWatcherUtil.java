/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.filewatcher.util;
/*
 * @author Aditya Jagtiani
 */

import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.AppPathMatcher;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.processors.CustomFileWalker;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map;

public class FileWatcherUtil {

    private static final Logger LOGGER = ExtensionsLoggerFactory.getLogger(FileWatcherUtil.class);

    public static void walk(String baseDirectory, PathToProcess pathToProcess, Map<String, FileMetric> fileMetrics)
            throws IOException {
        GlobPathMatcher globPathMatcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        if(pathToProcess.getPath().contains("**")){
            LOGGER.trace("FileWatcherUtil :: walk - feeding basedirectory "+baseDirectory+" path to walkFileTree");
            Files.walkFileTree(Paths.get(baseDirectory), new HashSet<>(), Integer.MAX_VALUE, new CustomFileWalker(baseDirectory, globPathMatcher, pathToProcess,
                    fileMetrics));
        } else if(pathToProcess.getPath().contains("*")) {
            LOGGER.trace("FileWatcherUtil :: walk - feeding basedirectory "+baseDirectory+" path to walkFileTree");
            Files.walkFileTree(Paths.get(baseDirectory), new HashSet<>(), 2, new CustomFileWalker(baseDirectory, globPathMatcher, pathToProcess,
                    fileMetrics));
        } else{
            LOGGER.trace("FileWatcherUtil :: walk - feeding basedirectory "+pathToProcess.getPath()+" path to walkFileTree");
            Files.walkFileTree(Paths.get(pathToProcess.getPath()), new HashSet<>(), 1, new CustomFileWalker(baseDirectory, globPathMatcher, pathToProcess,
                    fileMetrics));
        }
    }

    public static String getFormattedDisplayName(String fileDisplayName, Path path, String baseDir) {
        if (!baseDir.endsWith("/") || !baseDir.endsWith("\\")) {
            if (baseDir.contains("/")) {
                baseDir += "/";
            } else {
                baseDir += "\\";
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(fileDisplayName);
        String suffix = path.toString().replace(baseDir.substring(0, baseDir.length() - 1), "")
                .replace(File.separator, "|");
        if (!suffix.startsWith("|")) {
            builder.append('|');
            builder.append(suffix);
        } else {
            builder.append(suffix);
        }
        return builder.toString();
    }

    public static long getNumberOfLinesFromFile(Path file) {
        if (file.toFile().exists()) {
        	long lines = 0;
        	try {
        		BufferedReader reader = new BufferedReader(new FileReader(file.toFile().getAbsolutePath()));
                while (reader.readLine() != null) lines++;
                reader.close();
        		
        	} catch (IOException e) {
        		LOGGER.warn("IOException Occurred trying to read the file, possible permissions issue", e.getCause());
        		lines=-1;
        	}
            return lines;
        }
        return 0;
    }

    public static long calculateRecursiveFileCount(Path path, boolean ignoreHiddenFiles,
                                                   boolean excludeSubdirectoriesFromFileCounts) throws IOException {
        if (ignoreHiddenFiles) {
            if (!excludeSubdirectoriesFromFileCounts) {
                return Files.walk(path)
                        .parallel()
                        .filter(p -> (p.toFile().isFile()
                                || p.toFile().isDirectory())
                                && !p.toFile().isHidden())
                        .count() - 1;
            }
            return Files.walk(path)
                    .parallel()
                    .filter(p -> !p.toFile().isDirectory()
                            && !p.toFile().isHidden())
                    .count();
        } else {
            if (!excludeSubdirectoriesFromFileCounts) {
                return Files.walk(path)
                        .parallel()
                        .filter(p -> (p.toFile().isFile()
                                || p.toFile().isDirectory()))
                        .count() - 1;
            }
            return Files.walk(path)
                    .parallel()
                    .filter(p -> !p.toFile().isDirectory())
                    .count();
        }
    }

    public static long calculateRecursiveDirectoryCount(Path path, boolean ignoreHiddenFiles) throws IOException {
        int[] count = {0};
        Files.walkFileTree(path,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if(ignoreHiddenFiles && dir.toFile().isHidden()){
                    return FileVisitResult.CONTINUE;
                } else {
                    count[0]++;
                    return FileVisitResult.CONTINUE;
                }
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                return FileVisitResult.SKIP_SUBTREE;
            }

        });
        return count[0]-1;
    }

    public static AppPathMatcher getPathMatcher(PathToProcess fileToProcess) {
        AppPathMatcher matcher = new GlobPathMatcher();
        matcher.setMatcher(fileToProcess);
        return matcher;
    }

    public static boolean isDirectoryAccessible(Path path) {
        return Files.exists(path) && Files.isReadable(path) && Files.isExecutable(path)
                && Files.isDirectory(path);
    }

    public static boolean isFileAccessible(Path path) {
        return Files.exists(path) && Files.isReadable(path) && Files.isRegularFile(path);
    }
}