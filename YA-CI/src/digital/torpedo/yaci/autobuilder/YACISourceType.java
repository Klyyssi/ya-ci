/**
 *  YACI Source Types
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

import digital.torpedo.yaci.autobuilder.fileprocessers.Gitter;
import digital.torpedo.yaci.autobuilder.fileprocessers.Unzipper;
import java.util.Optional;

/**
 * @author Tuomo Heino
 * @version 19.1.2016
 */
public enum YACISourceType {
    /** Zip Container, uses Local Path */
    ZIP(new Unzipper()),
    /** Zip over HTTP */
    HTTP_ZIP(null),
    /** Git from remote */
    GIT(new Gitter());
    
    final FileProcesser processer;
    
    private YACISourceType(FileProcesser processer) {
        this.processer = processer;
    }
    
    public static Optional<YACISourceType> fromString(String sourceType) {
        switch (sourceType) {
            case "git":
                return Optional.of(YACISourceType.GIT);
            case "httpzip":
                return Optional.of(YACISourceType.HTTP_ZIP);
            case "zip":
                return Optional.of(YACISourceType.ZIP);
            default:
                return Optional.empty();
        }
    }
}