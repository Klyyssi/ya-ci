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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Tuomo Heino
 * @version 13.1.2016
 */
public class AutoBuilder {
    private static final Path buildQueue = Paths.get("build_queue"),
                              buildFinal = Paths.get("build_final");
    private static final int BUFFER_SIZE = 4096;
    
    private static String removeSuffix(Path p) {
        String name = p.getFileName().toString();
        int lastIndex = name.lastIndexOf('.');
        if(lastIndex == -1 || lastIndex == 0) return name;
        return name.substring(0, lastIndex);
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
     * @param p path to zip
     */
    public void submitZipFile(Path p) {
        Path current = buildQueue.resolve(removeSuffix(p)+"_"+stamp()+"/");
        
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(p.toFile()))) {
            Files.createDirectories(current);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            
            ZipEntry ze = zis.getNextEntry();
            while(ze != null) {
                String fileName = ze.getName();
                
                System.out.println("Unzipping: "+fileName);
                
                Path file = current.resolve(fileName);
                
                if(ze.isDirectory()) {
                    Files.createDirectories(file);
                } else {
                    if(!Files.exists(file.getParent()))
                        Files.createDirectories(file.getParent());
                    
                    try (FileOutputStream out = new FileOutputStream(file.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
                
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            System.out.println("Succesfully unzipped: "+p.getFileName().toString()+" to "+current.getFileName().toString());
        } catch(IOException ex){
            ex.printStackTrace(); 
        }
    }
    
    private static void createBase() {
        try {
            Files.createDirectories(buildQueue);
            Files.createDirectories(buildFinal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args arguments
     */
    public static void main(String[] args) {
        createBase();
    }
}
