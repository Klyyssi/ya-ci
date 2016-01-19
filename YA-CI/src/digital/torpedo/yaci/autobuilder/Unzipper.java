/**
 *  Unzipper implementation
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Tuomo Heino
 * @version 13.1.2016
 */
public class Unzipper implements FileProcesser {
    private static final int BUFFER_SIZE = 4096;
    
    @Override
    public Path processFile(String p, Path baseFolder, String stamp) {
        Path file = Paths.get(p);
        Path current = baseFolder.resolve(AutoBuilderImpl.removeSuffix(file)+"_"+stamp+"/");
        
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(file.toFile()))) {
            Files.createDirectories(current);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            
            ZipEntry ze = zis.getNextEntry();
            while(ze != null) {
                String fileName = ze.getName();
                
                System.out.println("Unzipping: "+fileName);
                
                file = current.resolve(fileName);
                
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
            System.out.println("Succesfully unzipped: "+file.getFileName().toString()+" to "+current.getFileName().toString());
            return current;
        } catch(IOException ex){
            ex.printStackTrace(); 
            return null;
        }
    }
}