/**
 *  Contains some general web server responses
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
import fi.iki.elonen.NanoHTTPD.Response;
import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

/**
 *
 * @author Markus Mulkahainen
 */
public class Responses {
    
    public static Response error404() {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "HTTP Error 404 - Page Not Found");        
    }
    
    public static Response errorWrongUriParameters() {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_HTML, 
                        "<!DOCTYPE html><html><body><h1>YA-CI</h1>"
                        + "<p>Failed to operate. Bad URL parameters.</p></body></html>");
    }
    
    public static Response somethingWentWrong() {
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_HTML,
                "<!DOCTYPE html><html><body><h1>YA-CI</h1>"
                + "<p>Oops! Something went wrong.</p></body></html>");
    }
}
