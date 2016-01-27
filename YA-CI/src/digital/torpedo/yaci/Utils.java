/**
 *  Utils class for static methods
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
package digital.torpedo.yaci;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utils class for static methods
 * @author Tuomo Heino
 * @version 22.1.2016
 */
public class Utils {
    
    /**
     * Removes Suffix from given paths filename and returns it
     * @param p path
     * @return paths filename with no suffix
     */
    public static String removeSuffix(Path p) {
        return removeSuffix(p.getFileName().toString());
    }
    
    /**
     * Removes Suffix from given path and returns it
     * @param p path
     * @return path with no suffix
     */
    public static String removeSuffix(String name) {
        int lastIndex = name.lastIndexOf('.');
        if(lastIndex == -1 || lastIndex == 0) return name;
        return name.substring(0, lastIndex);
    }
    
    /**
     * Tries to Create Given Folder and any parent folders
     * @param folder folder path
     * @return if succeeded
     */
    public static boolean createDirectories(Path folder) {
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    public static void writeException(Exception ex, Path p) {
        if(ex != null) {
            try(PrintWriter out = new PrintWriter(p.toFile())) {
                ex.printStackTrace(out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}