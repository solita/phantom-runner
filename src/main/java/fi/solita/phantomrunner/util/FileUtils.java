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
