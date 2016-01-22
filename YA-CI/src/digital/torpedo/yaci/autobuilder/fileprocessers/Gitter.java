/**
 *  Git Processer implementation
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
package digital.torpedo.yaci.autobuilder.fileprocessers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import digital.torpedo.yaci.autobuilder.FileProcesser;
import digital.torpedo.yaci.autobuilder.YACITask.YACITaskConf;

/**
 * @author Tuomo Heino
 * @version 13.1.2016
 */
public class Gitter implements FileProcesser {
    
    @Override
    public Path processFile(String p, Path baseFolder, String stamp, YACITaskConf config) {
        Path folder = baseFolder.resolve(getGitName(p) + "_" + stamp);
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Git repo = Git.cloneRepository().setURI(p).setDirectory(folder.toFile()).call()) {
            if(config.gitBranch != null) {
                repo.checkout().setName(config.gitBranch).call();
            }
            return folder;
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private String getGitName(String url) {
        int last = url.lastIndexOf('/');
        if(last == -1) return url.substring(0, url.lastIndexOf('.'));
        return url.substring(last+1, url.lastIndexOf('.'));
    }
}