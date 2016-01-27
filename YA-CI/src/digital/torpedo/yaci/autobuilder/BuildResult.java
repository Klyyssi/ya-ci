/**
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

import org.apache.maven.shared.invoker.InvocationResult;

/**
 * @author Tuomo Heino
 * @version 26.1.2016
 */
public class BuildResult {
    /** Exit Code */
    public final int exitCode;
    /** Build Log */
    public final String buildLog;
    /** Possible Exception wrapped with Optional */
    public final Optional<Exception> exception;
    
    /**
     * BuildResult
     * @param resCode result code
     * @param buildLog build log lines
     */
    public BuildResult(InvocationResult res, String buildLog) {
        this.exitCode = res.getExitCode(); this.buildLog = buildLog;
        this.exception = Optional.ofNullable(res.getExecutionException());
    }
}
