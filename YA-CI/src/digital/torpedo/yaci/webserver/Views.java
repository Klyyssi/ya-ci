/**
 *  View manager
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
package digital.torpedo.yaci.webserver;

import digital.torpedo.yaci.YACI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Markus Mulkahainen
 */
public class Views {
    
    private static final String VIEWS_PATH = "resources/views/";
        
    public static String get(String viewName) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream res = YACI.class.getResourceAsStream(VIEWS_PATH + viewName);
        if (res == null) throw new NullPointerException();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(res))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
