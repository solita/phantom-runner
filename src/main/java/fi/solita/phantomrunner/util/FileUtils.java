/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Solita Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fi.solita.phantomrunner.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.plexus.util.IOUtil;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.google.common.io.Files;

public class FileUtils {

    /**
     * Extracts the given resource to user's temporary directory (by creating a unique directory underneath
     * it). Will delete that file and created directories on system exit if deleteOnExit is true.
     * 
     * @param resource Resource to be extracted
     * @param subPath Slash (character '/') separated path of the sub-directories which should be created
     * @param deleteOnExit If the resource and created sub-directories should be deleted
     * 
     * @return File handle to the created file in the temporary directory
     */
    public static File extractResourceToTempDirectory(Resource resource, String subPath, boolean deleteOnExit) throws IOException {
        final File tempDir = Files.createTempDir();
        if (deleteOnExit) tempDir.deleteOnExit();
        
        File lastDir = tempDir;
        for (String subDir : subPath.split("/")) {
            // if the subPath starts or ends with '/' we'll get empty strings too
            if (StringUtils.hasLength(subDir)) {
                lastDir = new File(lastDir, subDir);
                lastDir.mkdir();
                if (deleteOnExit) lastDir.deleteOnExit();
            }
        }
        
        final File resFile = new File(lastDir, resource.getFilename());
        resFile.createNewFile();
        if (deleteOnExit) resFile.deleteOnExit();
        
        IOUtil.copy(resource.getInputStream(), new FileWriter(resFile), "UTF-8");
        
        return resFile;
    }
}
