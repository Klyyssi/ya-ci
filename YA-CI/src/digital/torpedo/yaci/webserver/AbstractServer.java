/**
 *  Very simple uri-to-handler mapping mechanism
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

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.Map;
import java.util.function.*;
import java.util.HashMap;

/**
 * @author Markus Mulkahainen
 */
public class AbstractServer extends NanoHTTPD {
    
    private final Map<String, Function<IHTTPSession, Response>> uriToHandler = new HashMap<>();

    public AbstractServer(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);        
    }
    
    public void addMapping(String route, Function<IHTTPSession, Response> handler) {
        uriToHandler.put(route, handler);
    }
    
    @Override
    public Response serve(IHTTPSession session) {
        Function<IHTTPSession, Response> handler = uriToHandler.get(session.getUri().substring(1));
        
        if (handler == null) {           
            return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "HTTP Error 404 - Page Not Found");        
        }
        
        return handler.apply(session);
    }
}
