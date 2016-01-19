/**
 *  Automatic Builder class
 *  
 *  Copyright (C) 2016  Tuomo Heino, Markus Mulkahainen
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, you can access it online at
 *  http://www.gnu.org/licenses/gpl-2.0.html.
 */
package digital.torpedo.yaci.autobuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.google.gson.Gson;

import digital.torpedo.yaci.autobuilder.YACIConfig.YACIConfigBlock;


/**
 * @author Tuomo Heino
 * @version 13.1.2016
 */
class AutoBuilderImpl implements AutoBuilder {
    private final Gson gson = new Gson();
    private final PriorityBlockingQueue<YACITask> buildQueue = new PriorityBlockingQueue<>(32, (a,b) -> 1);
    private final PriorityBlockingQueue<Path> cleanUpQueue = new PriorityBlockingQueue<>(16,(a,b)->1);
    private final Invoker invoker = new DefaultInvoker();
    private Path tempFolder, buildFolder;
    private Thread worker, cleaner;
    
    /**
     * Builder
     * @param mavenPath mavenpath
     */
    AutoBuilderImpl(String mavenPath, String tempFolder, String buildFolder) {
        this.tempFolder = Paths.get(tempFolder);
        this.buildFolder = Paths.get(buildFolder);
        this.invoker.setMavenHome(new File(mavenPath));
        
        this.worker = new Thread(this::queueThread, "AutoBuilder Worker");
        this.worker.setDaemon(true);
        this.worker.start();
        
        this.cleaner = new Thread(this::cleanUp, "AutoBuilder CleanUp");
        this.cleaner.setDaemon(true);
        this.cleaner.start();
    }
    
    @Override
    public void queueTask(YACITask task) {
        if(task == null) return;
        buildQueue.add(task);
    }
    
    private void queueThread() {
        System.out.println("Queue Thread Started!");
        while(true) {
            try {
                YACITask task = buildQueue.take();
                if(task == null) continue;
                System.out.println("Processing: "+task);
                Path projectBase = task.srcType.processer.processFile(task.source, tempFolder, stamp()); 
                Path[] outputs = resolveConfFiles(projectBase);
                if(outputs != null) {
                    for(Path output : outputs)
                        buildMe(output);
                }
                if(projectBase != null) {
                    cleanUpQueue.add(projectBase);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void cleanUp() {
        System.out.println("CleanUp Thread Started!");
        while(true) {
            try {
                Path clean = cleanUpQueue.take();
                if(clean == null) return;
                System.out.println("Starting Clean Up!");
                waitFor(5000);
                cleanUp(clean);
                System.out.println("Clean Up Done!");
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private Path[] resolveConfFiles(Path projectFolder) {
        if(projectFolder == null) return null;
        Path cfg = projectFolder.resolve(YACIConfig.YACI_CONFIG_NAME);
        if(!Files.exists(cfg)) return new Path[] {projectFolder};
        try(BufferedReader in = Files.newBufferedReader(cfg)) {
            YACIConfig config = gson.fromJson(in, YACIConfig.class);
            if(config != null && config.isValid()) {
                List<Path> paths = new ArrayList<>();
                for(YACIConfigBlock b : config) {
                    Path p = projectFolder.resolve(b.getFolder());
                    if(hasPom(p))
                        paths.add(p);
                }
                if(paths.size() == 0) return null;
                return paths.toArray(new Path[] {});
            }
            if(hasPom(projectFolder))
                return new Path[] {projectFolder};
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private boolean hasPom(Path folder) {
        return Files.exists(folder.resolve("pom.xml"));
    }
    
    private void buildMe(Path projectFolder) {
        System.out.println("Building: "+projectFolder.toString());
        InvocationRequest req = new DefaultInvocationRequest();
        req.setPomFile(projectFolder.resolve("pom.xml").toFile());
        req.setGoals(Collections.singletonList("package"));
        
        
        try {
            InvocationResult res = invoker.execute(req);
            if(res.getExitCode() != 0) {
                if(res.getExecutionException() != null)
                    res.getExecutionException().printStackTrace();
            } else {
                moveJars(projectFolder);
                System.out.println("Build Finished!");
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Tries to wait given time
     * @param ms
     */
    private void waitFor(long ms) {
        try {
            Thread.sleep(ms);
        } catch(Exception ex) {}
    }
    
    /**
     * Cleans Folder
     * @param projectFolder
     */
    private void cleanUp(Path projectFolder) {
        //TODO Won't delete git files properly!
        if(Files.isDirectory(projectFolder)) {
            try(DirectoryStream<Path> strm = Files.newDirectoryStream(projectFolder)) {
                strm.forEach(this::cleanUp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.deleteIfExists(projectFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String stampRegExp = ".+_\\d{17}";
    private String checkPathStamp(Path p) {
        String stamp = "_"+stamp();
        String str = p.toString();
        if(str.matches(stampRegExp))
            return str;
        return str+stamp;
    }

    private void moveJars(Path inputFolder) {
        Path target = inputFolder.resolve("target");
        if(!Files.exists(target)) {
            System.err.println("Maven Build Failed!");
            return;
        }
        
        Path baseBuild = buildFolder.resolve(checkPathStamp(inputFolder.getFileName()));
        if (!Files.exists(baseBuild)) {
            try {
                Files.createDirectories(baseBuild);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        try(DirectoryStream<Path> jarStrm = Files.newDirectoryStream(target, "*.jar")) {
            jarStrm.forEach(p -> move(p, baseBuild));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void move(Path file, Path base) {
        try {
            System.out.print("Moving file: "+file.getFileName().toString()+"... ");
            Files.copy(file, base.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            System.out.print("Success!"+System.lineSeparator());
        } catch (IOException e) {
            System.out.print("Failed!"+System.lineSeparator());
            e.printStackTrace();
        }
    }
    
    static String removeSuffix(Path p) {
        String name = p.getFileName().toString();
        int lastIndex = name.lastIndexOf('.');
        if(lastIndex == -1 || lastIndex == 0) return name;
        return name.substring(0, lastIndex);
    }
    
    private static DateTimeFormatter formatter = new DateTimeFormatterBuilder().
                                                     appendValue(ChronoField.DAY_OF_MONTH,     2).
                                                     appendValue(ChronoField.MONTH_OF_YEAR,    2).
                                                     appendValue(ChronoField.YEAR).
                                                     appendValue(ChronoField.HOUR_OF_DAY,      2).
                                                     appendValue(ChronoField.MINUTE_OF_HOUR,   2).
                                                     appendValue(ChronoField.SECOND_OF_MINUTE, 2).
                                                     appendValue(ChronoField.MILLI_OF_SECOND,  3).
                                                     toFormatter();
    private static String stamp() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }
    
    /**
     * @param args args
     */
    public static void main(String[] args) {
        AutoBuilder bldr = AutoBuilder.getInstance("C:\\maven\\", "temp/", "build/");
        //bldr.queueTask(new YACITask.YACITaskBuilder("TextAdventure.zip", YACISourceType.ZIP).build());
        bldr.queueTask(new YACITask("https://taavistain@bitbucket.org/taavistain/tekstiseikkailu.git", YACISourceType.GIT));
        try(Scanner sc = new Scanner(System.in)) {
            sc.nextLine();
        }
    }
}
