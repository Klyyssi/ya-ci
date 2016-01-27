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

import digital.torpedo.yaci.Config;
import digital.torpedo.yaci.autobuilder.AutoBuilder;
import digital.torpedo.yaci.autobuilder.YACISourceType;
import digital.torpedo.yaci.autobuilder.YACITask;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Optional;

/**
 *
 * @author Markus Mulkahainen
 */
public class WebServer extends AbstractServer {
    private static final String TEMP_PATH  = Config.getConfig().getTempPath();
    private static final String BUILD_PATH = Config.getConfig().getBuildPath();
    private static final String MAVEN_PATH = Config.getConfig().getMavenPath();
    
    private final AutoBuilder builder = AutoBuilder.getInstance(MAVEN_PATH, TEMP_PATH, BUILD_PATH);
    /** */

    public WebServer(int port) throws IOException {
        super(port);
        super.addMapping("", this::index);
        super.addMapping("index.html", this::index);
        super.addMapping("build", this::build);
    }
    
    public Response index(IHTTPSession session) {
        String response = "";
        try {
            response = Views.get("index").replaceAll("<ya-ci:TABLE>", createBuildTable());
        } catch (IOException ex) {
            System.err.println("Failed to get index file or create build table \n" + ex);
            return Responses.somethingWentWrong();
        }

        return newFixedLengthResponse(response);
    }

    public Response build(IHTTPSession session) {
        String response = "";
        String urlParam = session.getParms().get("url");
        String gitBranch = session.getParms().get("branch");
        Optional<YACISourceType> sourceType = YACISourceType.fromString(session.getParms().get("sourcetype"));
        
        if (!sourceType.isPresent() || urlParam == null) {
            return Responses.errorWrongUriParameters();
        }
        if (sourceType.get() == YACISourceType.GIT && gitBranch == null) {
            return Responses.errorWrongUriParameters();
        }
            
        builder.queueTask(new YACITask.YACITaskBuilder(urlParam, sourceType.get()).gitBranch(gitBranch).build());
        
        try {
            response = Views.get("build").replaceAll("<ya-ci:BUILD_MSG>", "Project is being built from " + urlParam + "...");
        } catch (IOException ioe) {
            System.err.println("Failed to get build view \n" + ioe);
            return Responses.somethingWentWrong();
        }
        
        return newFixedLengthResponse(response);
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
                            .append("</td><td>")
                            .append(new SimpleDateFormat("dd.MM.yyyy H:mm").format(new Date(x.toFile().lastModified())))
                            .append("</td><td><a href='")
                            .append(x.toUri().toString())
                            .append("'>Download</a></td></tr>"),
                        StringBuilder::append,
                        StringBuilder::toString)));
                
        return s.append("</table>").toString();
    }
}
