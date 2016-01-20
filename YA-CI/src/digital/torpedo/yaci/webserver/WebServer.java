/**
 *  NanoHttpd WebServer
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

import digital.torpedo.yaci.autobuilder.AutoBuilder;
import digital.torpedo.yaci.autobuilder.YACISourceType;
import digital.torpedo.yaci.autobuilder.YACITask;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collector;

/**
 *
 * @author Markus Mulkahainen
 */
public class WebServer extends NanoHTTPD {
    
    private final AutoBuilder builder = AutoBuilder.getInstance(MAVEN_PATH, TEMP_PATH, BUILD_PATH);
    
    /** FOR TEST PURPOSES ONLY */
    private static final String TEMP_PATH = "/home/markus/testi/temp";
    private static final String BUILD_PATH = "/home/markus/testi/build";
    private static final String MAVEN_PATH = "/usr/share/maven";
    /** */

    public WebServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);        
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>YA-CI</h1>\n";
        Map<String, String> parms = session.getParms();

        if (parms.get("git") == null) {
            msg += "<form action='?' id='formGit' method='get'>\n  <p>Git URL: <input type='text' name='git'></p>\n" + "</form>\n"
                    + "<button type='submit' form='formGit'>Build</button>\n";
            try {
                msg += createBuildTable();
            } catch (IOException ex) {
                System.err.println("Couldn't create build table " + ex);
            }
        } else {
            String gitParam = parms.get("git");
            builder.queueTask(new YACITask.YACITaskBuilder(gitParam, YACISourceType.GIT).build());
            msg += "<p>Project is being built from " + gitParam + " [master]...</p>\n<a href='/'>Go back</a>";            
        }
        return newFixedLengthResponse(msg + "</body></html>\n");
    }
    
    private String createBuildTable() throws IOException {
        final StringBuilder s = new StringBuilder("<h2>Builds</h2><table>");
        
        s.append(Files.find(Paths.get(BUILD_PATH), 1, (path, attributes) -> attributes.isDirectory())
                .filter(x -> !x.endsWith(Paths.get(BUILD_PATH)))
                .collect(Collector.of(
                        StringBuilder::new, 
                        (sb, x) -> sb
                            .append("<tr><td>")
                            .append(x.getFileName().toString())
                            .append("</td><td><a href='")
                            .append(x.toUri().toString())
                            .append("'>Download</a></td></tr>"),
                        StringBuilder::append,
                        StringBuilder::toString)));
                
        return s.append("</table>").toString();
    }
}
