/**
 *  Main Config File
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

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Easily Modifiable Configuration File for YACI Webserver
 * @author Tuomo Heino
 * @version 26.1.2016
 */
public class Config {
    private static final String CFG_PATH = "config.json";
    private static final Gson GSON = new Gson();

    private static Config instance;

    /**
     * Returns Config Instance<br>
     * Don't keep own references to this instance as reset() can invalidate it!
     * @return config singleton instance
     */
    public static Config getConfig() {
        if(instance == null) {
            Path p = Paths.get(CFG_PATH);
            if(Files.exists(p)) {
                try(BufferedReader in = Files.newBufferedReader(p)) {
                    instance = GSON.fromJson(in, Config.class);
                    if(instance == null)
                        createInstance();
                } catch(IOException ex) {
                    ex.printStackTrace();
                    createInstance();
                }
            } else {
                createInstance();
            }
        }
        return instance;
    }

    private static void createInstance() {
        instance = new Config();
        instance.save();
    }

    /**
     * Reset Current Config Instance and Forces it to reload itself
     */
    public static void reset() {
        instance = null;
    }

    /** Do NOT use! Only for GSON! */
    Config() {
    }

    /**
     * Saves Config to a File
     */
    public void save() {
        try(BufferedWriter out = Files.newBufferedWriter(Paths.get(CFG_PATH), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(this, out);
        }catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
     * Place Wanted Config Attributes Below, Preferably add a default value!
     * Remember to create getters and setters for them!
     */
     private String tempPath  = "temp/",
                    buildPath = "build/",
                    mavenPath = "maven/";
     
     /*
      * Validator Below
      */
     public Exception validate() {
    	 return null;
     }
     
     /*
      * Getters And Setters Below!
      */
    /**
     * @param tempPath temp path to set
     */
    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    /**
     * @return temp path
     */
    public String getTempPath() {
        return tempPath;
    }

    /**
     * @param buildPath build path to set
     */
    public void setBuildPath(String buildPath) {
        this.buildPath = buildPath;
    }
    /**
     * @return build path
     */
    public String getBuildPath() {
        return buildPath;
    }

    /**
     * @param mavenPath maven path to set
     */
    public void setMavenPath(String mavenPath) {
        this.mavenPath = mavenPath;
    }
    /**
     * @return maven path
     */
    public String getMavenPath() {
        return mavenPath;
    }
}
