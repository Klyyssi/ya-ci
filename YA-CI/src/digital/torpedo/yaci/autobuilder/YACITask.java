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

import java.util.Optional;
import java.util.function.Consumer;

/**
 * QueueFile, Comparing always results 0
 * @author Tuomo Heino
 * @version 19.1.2016
 */
public class YACITask implements Comparable<YACITask> {
    final String source;
    final YACISourceType srcType;
    final YACITaskConf conf;
    
    YACITask(String source, YACISourceType srcType, String gitBranch, Consumer<String> buildOutputPipe, YACICallback callback) {
        this.source = source; this.srcType = srcType;
        this.conf = new YACITaskConf(gitBranch, buildOutputPipe, callback);
    }
    
    @Override
    public int compareTo(YACITask o) {
        return 0;
    }
    
    /**
     * Class to hold public properties which are given to FileProcessers
     * @author Tuomo Heino
     * @version 22.1.2016
     */
    public static class YACITaskConf {
        /** Git Branch to use */
        public final Optional<String> gitBranch;
        /** Output Pipe for Build Processes Messages */
        public final Optional<Consumer<String>> buildOutputPipe;
        /** Callback */
        public final Optional<YACICallback> callback;
        
        YACITaskConf(String gitBranch, Consumer<String> buildOutputPipe, YACICallback callback) {
            this.gitBranch = Optional.ofNullable(gitBranch);
            this.buildOutputPipe = Optional.ofNullable(buildOutputPipe);
            this.callback = Optional.ofNullable(callback);
        }
    }
    
    
    /**
     * @author Tuomo Heino
     * @version 19.1.2016
     */
    public static class YACITaskBuilder {
        private String source;
        private YACISourceType srcType;
        private String gitBranch;
        private Consumer<String> buildOutputPipe;
        private YACICallback callback;
        
        /**
         * @param source source
         * @param srcType source type
         */
        public YACITaskBuilder(String source, YACISourceType srcType) {
            this.source = source; this.srcType = srcType;
        }
        
        /**
         * Sets Git Branch
         * @param gitBranch git branch
         * @return this builder for chaining
         */
        public YACITaskBuilder gitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
            return this;
        }
        
        /**
         * Build Process will pipe it's output to given consumer
         * @param buildOutPipe build output
         * @return this builder for chaining
         */
        public YACITaskBuilder buildOutputPipe(Consumer<String> buildOutputPipe) {
            this.buildOutputPipe = buildOutputPipe;
            return this;
        }
        
        /**
         * Callback for build process, returns with BuildResult object
         * @param callback callback
         * @return this builder for chaining
         */
        public YACITaskBuilder callback(YACICallback callback) {
            this.callback = callback;
            return this;
        }
        
        /**
         * @return builds YACITask
         */
        public YACITask build() {
            return new YACITask(source, srcType, gitBranch, buildOutputPipe, callback);
        }
    }
}