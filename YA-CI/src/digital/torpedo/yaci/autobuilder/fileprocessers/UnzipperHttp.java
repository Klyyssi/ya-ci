/**
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
package digital.torpedo.yaci.autobuilder.fileprocessers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import digital.torpedo.yaci.Utils;
import digital.torpedo.yaci.autobuilder.FileProcesser;

/**
 * @author Tuomo Heino
 * @version 22.1.2016
 */
public class UnzipperHttp implements FileProcesser {
    private static final int BUFFER_SIZE = 4096;
    
    
    @Override
    public Path processFile(String p, Path baseFolder, String stamp) {
        try {
            URL url = new URL(p);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = con.getHeaderField("Content-Disposition");
                String contentType = con.getContentType();
                int contentLength = con.getContentLength();
                
                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    fileName = p.substring(p.lastIndexOf("/") + 1);
                }
                
                String folder = Utils.removeSuffix(fileName)+"_"+stamp;
                
                System.out.println("Content-Type = " + contentType);
                System.out.println("Content-Disposition = " + disposition);
                System.out.println("Content-Length = " + contentLength);
                System.out.println("fileName = " + fileName);
                
                Path outFolder = baseFolder.resolve(folder);
                if(!Files.exists(outFolder))
                    Files.createDirectories(outFolder);
                
                Path save = outFolder.resolve(fileName);
                try(InputStream inputStream = con.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(save.toFile());) {
                    int bytesRead = -1;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("File downloaded");
                con.disconnect();
                return Unzipper.unzip(save.toString(), baseFolder, stamp);
            }
            con.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}