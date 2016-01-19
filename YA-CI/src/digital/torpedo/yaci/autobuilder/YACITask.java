/**
 *  The MIT License (MIT)
 *
 * Copyright (c) 2015 Tuomo Heino
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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