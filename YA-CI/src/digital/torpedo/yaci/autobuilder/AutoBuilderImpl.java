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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import com.google.gson.Gson;

import digital.torpedo.yaci.Utils;
import digital.torpedo.yaci.autobuilder.YACIConfig.YACIConfigBlock;
import digital.torpedo.yaci.autobuilder.YACITask.YACITaskConf;

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
                String stamp = stamp();
                Path projectBase = task.srcType.processer.processFile(task.source, tempFolder, stamp, task.conf); 
                Path[] outputs = resolveConfFiles(projectBase);
                if(outputs != null) {
                    List<BuildResult> buildResults = new ArrayList<>();
                    for(Path output : outputs)
                        buildResults.add(buildMe(output, task.conf));
                    
                    task.conf.callback.ifPresent(c -> c.callback(buildResults));
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
                cleanUpDuty(clean).ifPresent(cleanUpQueue::add);
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
    
    private BuildResult buildMe(Path projectFolder, YACITaskConf conf) {
        System.out.println("Starting Maven Build: "+projectFolder.toString());
        
        InvocationRequest req = new DefaultInvocationRequest();
        req.setPomFile(projectFolder.resolve("pom.xml").toFile());
        req.setGoals(Collections.singletonList("package"));
        
        Path baseBuild = buildFolder.resolve(checkPathStamp(projectFolder.getFileName()));
        StringBuilder log = new StringBuilder();
        
        try {
            invoker.setOutputHandler(line -> {
                System.out.println(line);
                log.append(line).append(System.lineSeparator());
                conf.buildOutputPipe.ifPresent(p -> p.accept(line));
            });
            
            InvocationResult res = invoker.execute(req);
            
            if(res.getExitCode() != 0) {
                if(res.getExecutionException() != null)
                    res.getExecutionException().printStackTrace();
                
                return writeBuildLogs(baseBuild, new BuildResult(res, log.toString()));
            } 
            
            Path target = projectFolder.resolve("target");
            if (!Files.exists(target)) {
                System.err.println("Maven Build Failed!");
                return writeBuildLogs(baseBuild, new BuildResult(BuildResult.INTERNAL_ERROR, new FileNotFoundException("No Target Folder Found!"), "Maven Build Failed!"));
            }
            Utils.createDirectories(baseBuild);
            
            try (DirectoryStream<Path> jarStrm = Files.newDirectoryStream(target, "*.jar")) {
                jarStrm.forEach(p -> move(p, baseBuild));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Maven Build Finished!");
            return writeBuildLogs(baseBuild, new BuildResult(res, log.toString()));
        } catch (MavenInvocationException e) {
            System.err.println("Maven Build Failed!");
            e.printStackTrace();
            return writeBuildLogs(baseBuild, new BuildResult(BuildResult.INTERNAL_ERROR, e, "ERROR: "+e.getMessage()));
        }
    }
    
    private BuildResult writeBuildLogs(Path baseBuild, BuildResult result) {
        String stamp = stamp();
        try(BufferedWriter out = Files.newBufferedWriter(baseBuild.resolve("build_"+stamp+".log"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            out.write("Exit Code: "+result.exitCode);
            out.newLine();
            out.write(result.buildLog);
            out.newLine();
        } catch(IOException e) {
            e.printStackTrace();
        }
        result.exception.ifPresent(ex -> Utils.writeException(ex, baseBuild.resolve("error_"+stamp+".log")));
        return result;
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
    
    private Optional<Path> cleanUpDuty(Path cleanMe) {
        List<Exception> exceptions = new ArrayList<>();
        cleanUp(cleanMe, exceptions);
        if(exceptions.isEmpty()) return Optional.empty();
        return Optional.ofNullable(cleanMe);
    }
    
    /**
     * Cleans Folder
     * @param projectFolder
     */
    private void cleanUp(Path projectFolder, List<Exception> ex) {
        //TODO Won't delete git files properly!
        if(Files.isDirectory(projectFolder)) {
            try(DirectoryStream<Path> strm = Files.newDirectoryStream(projectFolder)) {
                strm.forEach(f -> cleanUp(f, ex));
            } catch (IOException e) {
                System.err.println(e.getMessage());
                ex.add(e);
            }
        }
        try {
            Files.deleteIfExists(projectFolder);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            ex.add(e);
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
        //bldr.queueTask(new YACITask.YACITaskBuilder("TextAdventure.zip", YACISourceType.ZIP).callback(list -> System.out.println(list.size())).build());
        bldr.queueTask(new YACITask.YACITaskBuilder("https://taavistain@bitbucket.org/taavistain/tekstiseikkailu.git", YACISourceType.GIT).gitBranch("develop").build());
        try(Scanner sc = new Scanner(System.in)) {
            sc.nextLine();
        }
    }
}
