/**
 *  The MIT License (MIT)
 *
 * Copyright (c) 2015 Tuomo Heino
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
        Path current = baseFolder.resolve(AutoBuilder.removeSuffix(file)+"_"+stamp+"/");
        
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