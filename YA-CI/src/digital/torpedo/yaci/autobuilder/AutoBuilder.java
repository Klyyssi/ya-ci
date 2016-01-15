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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class AutoBuilder {
    private final Gson gson = new Gson();
    private final PriorityBlockingQueue<String> buildQueue = new PriorityBlockingQueue<>(32, (a,b) -> 1);
    private final Map<String, FileProcesser> fileProcessers = new HashMap<>();
    private final Invoker invoker = new DefaultInvoker();
    private Path tempFolder, buildFolder;
    private Thread worker;
    
    /**
     * Builder
     */
    public AutoBuilder() {
        this.tempFolder = Paths.get("temp/");
        this.buildFolder = Paths.get("build/");
        this.invoker.setMavenHome(new File("C:\\maven\\"));
        
        fileProcessers.put("zip", new Unzipper());
        fileProcessers.put("git", new Gitter());
        
        this.worker = new Thread(this::queueThread, "AutoBuilder Worker");
        this.worker.setDaemon(true);
        this.worker.start();
    }
    
    private void queueThread() {
        System.out.println("Queue Thread Started!");
        while(true) {
            try {
                String p = buildQueue.take();
                if(p == null) continue;
                System.out.println("Processing: "+p);
                Path projectBase = extractMe(p); 
                Path[] outputs = resolveConfFiles(projectBase);
                if(outputs != null) {
                    for(Path output : outputs)
                        buildMe(output);
                }
                if(projectBase != null)
                    cleanUp(projectBase);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * @param folder folder to set unzip/git project bases
     */
    public void setTempFolder(Path folder) {
        this.tempFolder = folder;
    }
    
    /**
     * @param folder folder to place builded jars
     */
    public void setBuildFolder(Path folder) {
        this.buildFolder = folder;
    }
    
    /**
     * Adds path/url to queue
     * @param pathUrl path/url to add
     */
    public void queueFile(String pathUrl) {
        if(pathUrl == null || pathUrl.isEmpty()) return;
        buildQueue.add(pathUrl);
    }
    
    private Path extractMe(String output) {
        String suffix = getSuffix(output);
        System.out.println("Suffix: "+suffix);
        if(fileProcessers.containsKey(suffix))
            return fileProcessers.get(suffix).processFile(output, tempFolder, stamp());
        System.out.println("No Processer found for: "+suffix);
        return null;
    }
    
    private Path[] resolveConfFiles(Path projectFolder) {
        if(projectFolder == null) return null;
        Path cfg = projectFolder.resolve(YACIConfig.YACI_CONFIG_NAME);
        if(Files.exists(cfg)) return new Path[] {projectFolder};
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
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Cleans Folder
     * @param projectFolder
     */
    private void cleanUp(Path projectFolder) {
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

    private void moveJars(Path inputFolder) {
        Path target = inputFolder.resolve("target");
        if(!Files.exists(target)) {
            System.err.println("Maven Build Failed!");
            return;
        }
        Path baseBuild = buildFolder.resolve(inputFolder.getFileName());
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
    
    private static String getSuffix(String s) {
        int lastIndex = s.lastIndexOf('.');
        if(lastIndex == -1 || lastIndex == 0) return "";
        return s.substring(lastIndex+1);
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
     * @param args arguments
     */
    public static void main(String[] args) {
        AutoBuilder ap = new AutoBuilder();
        //ap.queueFile("TextAdventure.zip");
        ap.queueFile("https://taavistain@bitbucket.org/taavistain/tekstiseikkailu.git");
        new Scanner(System.in).nextLine(); //Stops from quiting before hand
    }
    
}
