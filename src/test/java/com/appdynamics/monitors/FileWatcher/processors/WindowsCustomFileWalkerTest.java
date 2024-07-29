/*
 *  Copyright 2020. AppDynamics LLC and its affiliates.
 *  All Rights Reserved.
 *  This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *  The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.monitors.FileWatcher.processors;


import com.appdynamics.extensions.filewatcher.config.FileMetric;
import com.appdynamics.extensions.filewatcher.config.PathToProcess;
import com.appdynamics.extensions.filewatcher.helpers.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.processors.CustomFileWalker;
import com.appdynamics.extensions.filewatcher.util.FileWatcherUtil;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.filewatcher.util.FileWatcherUtil.getFormattedDisplayName;
import static org.powermock.api.mockito.PowerMockito.spy;

/*
 * @author Aditya Jagtiani
 */
@PrepareForTest({FileWatcherUtil.class, CustomFileWalker.class})
@RunWith(PowerMockRunner.class)
public class WindowsCustomFileWalkerTest {

    private CustomFileWalker classUnderTest;


    @Before
    public void setup() {
        spy(FileWatcherUtil.class);

    }


    @Test
    public void visitDirectoryNonRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\B\\\\C");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\B\\C\\D.TXT");
        Path b = Paths.get("A\\B\\C");
        List<Path> paths = Arrays.asList(a, b);

        for(Path p : paths) {

            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("A\\B\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            if (p.toString().equals("A\\B\\C")) {
                classUnderTest.preVisitDirectory(p, null);
            } else {
                classUnderTest.visitFile(p, null);
            }
        }

        Assert.assertEquals(1, fileMetrics.size());
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a, "A\\B\\")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b, "A\\B\\")));
    }

    @Test
    public void visitDirectoryAndContentsRecursivelyNoMatches() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\D\\\\**");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\Aditya\\a.log");
        Path b = Paths.get("A\\D5\\Testing\\machine-agent.log");
        Path c = Paths.get("A\\This\\Is\\Too\\Much");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("A\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            if (p.toString().equals("A\\This\\Is\\Too\\Much")) {
                classUnderTest.preVisitDirectory(p, null);
            } else {
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(0, fileMetrics.size());
        for (Path p : paths) {
            Assert.assertTrue(!fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), p,
                    "A\\")));
        }
    }

    @Test
    public void visitDirectoryAndContentsRecursively() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\D\\\\**");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\D\\a.log");
        Path b = Paths.get("A\\D\\Testing\\machine-agent.log");
        Path c = Paths.get("A\\D\\This\\Is\\Too\\Much");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("A\\D\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            if (p.toString().equals("A\\D\\This\\Is\\Too\\Much")) {
                classUnderTest.preVisitDirectory(p, null);
            } else {
                classUnderTest.visitFile(p, null);
            }
        }
        Assert.assertEquals(3, fileMetrics.size());
        for (Path p : paths) {
            Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), p,
                    "A\\D\\")));
        }
    }

    @Test
    public void visitFilesWithinADirectoryRegex() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\D\\\\*.log");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\D\\a.log");
        Path b = Paths.get("A\\D\\machine-agent.log");
        Path c = Paths.get("A\\D\\NoMatch.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("A\\D\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            classUnderTest.visitFile(p, null);
        }

        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a,
                "A\\D\\")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b,
                "A\\D\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c,
                "A\\D\\")));
    }

    @Test
    public void visitFileWithinADirectory() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\D\\\\a.log");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\D\\a.log");
        Path b = Paths.get("A\\D\\machine-agent.log");
        Path c = Paths.get("A\\D\\NoMatch.txt");
        List<Path> paths = Arrays.asList(a, b, c);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("A\\D\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            classUnderTest.visitFile(p, null);
        }

        Assert.assertEquals(1, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a,
                "A\\D\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b,
                "A\\D\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c,
                "A\\D\\")));
    }

    @Test
    public void visitFilesWithRegexesWithinADirectoryWithIntermediateRegexes() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("A\\\\D\\\\Air*\\\\*.log");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("A\\D\\Air Jordan 1\\a.log");
        Path b = Paths.get("A\\D\\Air Jordan 3\\machine-agent.log");
        Path c = Paths.get("A\\D\\Adidas\\NoMatch.txt");
        Path d = Paths.get("A\\D\\Air Jordan 1\\1985");
        List<Path> paths = Arrays.asList(a, b, c, d);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("A\\D\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            if (p.toString().equals("A\\D\\Air Jordan 1\\1985")) {
                classUnderTest.preVisitDirectory(p, null);
            } else {
                classUnderTest.visitFile(p, null);
            }
        }

        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a,
                "A\\D\\")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b,
                "A\\D\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c,
                "A\\D\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), d,
                "A\\D\\")));
    }

    @Test
    public void visitFilesWithRegexesWithinADirectoryWithIntermediateAndStartingRegexes() throws Exception {
        PathToProcess pathToProcess = new PathToProcess();
        pathToProcess.setDisplayName("Testing");
        pathToProcess.setPath("*\\\\A\\\\D\\\\Air*\\\\*.log");
        pathToProcess.setExcludeSubdirectoryCount(false);
        pathToProcess.setEnableRecursiveFileCounts(false);
        pathToProcess.setIgnoreHiddenFiles(true);

        Path a = Paths.get("\\A\\D\\Air Jordan 1\\a.log");
        Path b = Paths.get("\\A\\D\\Air Jordan 3\\machine-agent.log");
        Path c = Paths.get("\\A\\D\\Adidas\\NoMatch.txt");
        Path d = Paths.get("\\A\\D\\Air Jordan 1\\1985");
        List<Path> paths = Arrays.asList(a, b, c, d);

        for(Path p : paths) {
            PowerMockito.when(FileWatcherUtil.isDirectoryAccessible(p)).thenReturn(true);
            PowerMockito.when(FileWatcherUtil.isFileAccessible(p)).thenReturn(true);
        }

        GlobPathMatcher matcher = (GlobPathMatcher) FileWatcherUtil.getPathMatcher(pathToProcess);
        Map<String, FileMetric> fileMetrics = Maps.newHashMap();

        classUnderTest = new CustomFileWalker("\\", matcher, pathToProcess, fileMetrics);

        for (Path p : paths) {
            if (p.toString().equals("\\A\\D\\Air Jordan 1\\1985")) {
                classUnderTest.preVisitDirectory(p, null);
            } else {
                classUnderTest.visitFile(p, null);
            }
        }

        Assert.assertEquals(2, fileMetrics.size());
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), a,
                "\\")));
        Assert.assertTrue(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), b,
                "\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), c,
                "\\")));
        Assert.assertFalse(fileMetrics.containsKey(getFormattedDisplayName(pathToProcess.getDisplayName(), d,
                "\\")));
    }
}