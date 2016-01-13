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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;


/**
 * @author Tuomo Heino
 * @version 13.1.2016
 */
public class AutoBuilder {
    private final PriorityBlockingQueue<String> buildQueue = new PriorityBlockingQueue<>(32, (a,b) -> 1);
    private final Map<String, FileProcesser> fileProcessers = new HashMap<>();
    private Path tempFolder, buildFolder;
    private Thread worker;
    
    /**
     * Builder
     */
    public AutoBuilder() {
        this.tempFolder = Paths.get("temp/");
        this.buildFolder = Paths.get("build/");
        this.worker = new Thread(this::queueThread, "AutoBuilder Worker");
        this.worker.setDaemon(true);
        this.worker.start();
        
        fileProcessers.put("zip", new Unzipper());
        fileProcessers.put("git", new Gitter());
    }
    
    private void queueThread() {
        while(true) {
            try {
                String p = buildQueue.take();
                if(p == null) continue;
                Path output = extractMe(p);
                if(output != null)
                    buildMe(output);
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
    
    private Path extractMe(String output) {
        String suffix = getSuffix(output);
        if(fileProcessers.containsKey(suffix))
            return fileProcessers.get(suffix).processFile(output, tempFolder, stamp());
        return null;
    }
    
    private void buildMe(Path projectFolder) {
        InvocationRequest req = new DefaultInvocationRequest();
        req.setPomFile(projectFolder.resolve("pom.xml").toFile());
        req.setGoals(Collections.singletonList("package"));
        
        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult res = invoker.execute(req);
            if(res.getExitCode() != 0) {
                res.getExecutionException().printStackTrace();
            }
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }
    }
    
    static String removeSuffix(Path p) {
        String name = p.getFileName().toString();
        int lastIndex = name.lastIndexOf('.');
        if(lastIndex == -1 || lastIndex == 0) return name;
        return name.substring(0, lastIndex+1);
    }
    
    private static String getSuffix(String s) {
        int lastIndex = s.lastIndexOf('.');
        if(lastIndex == -1 || lastIndex == 0) return "";
        return s.substring(lastIndex);
    }
    
    private static DateTimeFormatter formatter = new DateTimeFormatterBuilder().
                                                     appendValue(ChronoField.DAY_OF_MONTH,     2).
                                                     appendValue(ChronoField.MONTH_OF_YEAR,    2).
                                                     appendValue(ChronoField.YEAR).
                                                     appendLiteral('_').
                                                     appendValue(ChronoField.HOUR_OF_DAY,      2).
                                                     appendValue(ChronoField.MINUTE_OF_HOUR,   2).
                                                     appendValue(ChronoField.SECOND_OF_MINUTE, 2).
                                                     appendLiteral('_').
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
        
    }
    
}
