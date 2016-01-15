/**
 *  Read-Only Config File
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

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import digital.torpedo.yaci.autobuilder.YACIConfig.YACIConfigBlock;

/**
 * @author Tuomo Heino
 * @version 15.1.2016
 */
public class YACIConfig implements Iterable<YACIConfigBlock> {
    /** Config File Name */
    public static final String YACI_CONFIG_NAME = "yaci.json";
    
    private final Set<YACIConfigBlock> blocks;
    
    
    /**
     * Empty Constructor for Reflection.<br>
     * Not usable as is.
     */
    public YACIConfig() {
        blocks = null;
    }
    
    @Override
    public void forEach(Consumer<? super YACIConfigBlock> action) {
        blocks.forEach(action);
    }

    @Override
    public Iterator<YACIConfigBlock> iterator() {
        return blocks.iterator();
    }
    
    /**
     * @return block amount
     */
    public int size() {
        return blocks.size();
    }
    
    /**
     * Config must have blocks to be valid
     * @return if is valid config
     */
    public boolean isValid() {
        return blocks != null && blocks.size() > 0;
    }
    
    /**
     * @author Tuomo Heino
     * @version 15.1.2016
     */
    public static class YACIConfigBlock {
        private final String folder;
        
        /**
         * Empty Constructor for Reflection.<br>
         * Not usable as is.
         */
        public YACIConfigBlock() {
            this.folder = null;
        }

        /**
         * @return the folder
         */
        public String getFolder() {
            return folder;
        }
    }
}