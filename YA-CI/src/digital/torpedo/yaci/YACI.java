/**
 *  Yet another continous integratio main class
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

import digital.torpedo.yaci.webserver.WebServer;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Tuomo Heino
 * @author Markus Mulkahainen
 * @version 13.1.2016
 */
public class YACI {
    /** YA-CI Main Version */
    public static final String VERSION = "v0.0.2";
    
    /** YA-CI web server port */
    public static final int WEB_SERVER_PORT = 8080;
    
    /**
     * @param args arguments
     */
    public static void main(String[] args) {
    	Optional<Exception> result = Optional.ofNullable(Config.getConfig().validate());
    	
    	result.ifPresent(Exception::printStackTrace);
    	
    	if(!result.isPresent())
    		run();
    }
    
    private static void run() {
        System.out.println("#================================================#");
        System.out.println("#=============Starting YA-CI "+VERSION+"!=============#");
        System.out.println("#                                                #");
        try {
            WebServer webServer = new WebServer(WEB_SERVER_PORT);
            System.out.println("#==NanoHTTPD Web server listening to port "+webServer.getListeningPort()+"!==#");
            System.out.println("#================================================#");
        } catch (IOException ex) {
            System.err.println("Couldn't start server: \n" +ex);
        }
    }
}