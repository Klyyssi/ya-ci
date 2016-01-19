/**
 *  YACITask and YACITaskBuilder classes
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

/**
 * QueueFile, Comparing always results 0
 * @author Tuomo Heino
 * @version 19.1.2016
 */
public class YACITask implements Comparable<YACITask> {
    final String source;
    final YACISourceType srcType;
    
    YACITask(String source, YACISourceType srcType) {
        this.source = source; this.srcType = srcType;
    }
    
    @Override
    public int compareTo(YACITask o) {
        return 0;
    }
    
    
    /**
     * @author Tuomo Heino
     * @version 19.1.2016
     */
    public static class YACITaskBuilder {
        private String source;
        private YACISourceType srcType;
        
        /**
         * @param source source
         * @param srcType source type
         */
        public YACITaskBuilder(String source, YACISourceType srcType) {
            this.source = source; this.srcType = srcType;
        }
        
        /**
         * @return builds YACITask
         */
        public YACITask build() {
            /*
            if(source == null) throw new YACITaskException("Source cannot be NULL!");
            if(srcType == null) throw new YACITaskException("Source Type cannot be NULL!");
            */
            return new YACITask(source, srcType);
        }
    }
}