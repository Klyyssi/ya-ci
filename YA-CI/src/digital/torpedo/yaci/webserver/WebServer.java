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
import digital.torpedo.yaci.autobuilder.Gitter;
import digital.torpedo.yaci.autobuilder.YACISourceType;
import digital.torpedo.yaci.autobuilder.YACITask;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Markus Mulkahainen
 */
public class WebServer extends NanoHTTPD {
    
    private AtomicInteger runningNumber = new AtomicInteger();
    
    /** FOR TEST PURPOSES ONLY */
    private static final String LOCAL_REPO_PATH_ROOT = "/home/markus/testi";
    private static final String TEMP_PATH = "/home/markus/testi/temp";
    private static final String BUILD_PATH = "/home/markus/testi/build";
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
        } else {
            String gitParam = parms.get("git");
            msg += "<p>Cloned and built " + gitParam + " [master]!</p>";
            AutoBuilder builder = AutoBuilder.getInstance("/usr/share/maven", TEMP_PATH, BUILD_PATH);
            builder.queueTask(new YACITask.YACITaskBuilder(gitParam, YACISourceType.GIT).build());
        }
        return newFixedLengthResponse(msg + "</body></html>\n");
    }
}
