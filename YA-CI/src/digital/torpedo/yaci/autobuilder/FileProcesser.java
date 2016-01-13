/**
 *  FileProcessing interface
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

/**
 * @author Tuomo Heino
 * @version 13.1.2016
 */
public interface FileProcesser {
    /**
     * Processes given path
     * @param p path/url
     * @param baseFolder folder to output base
     * @param stamp stamp to use
     * @return output folder
     */
    public Path processFile(String p, Path baseFolder, String stamp);
}