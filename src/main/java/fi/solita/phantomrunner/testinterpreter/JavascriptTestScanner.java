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
package fi.solita.phantomrunner.testinterpreter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import fi.solita.phantomrunner.PhantomConfiguration;
import fi.solita.phantomrunner.util.ClassUtils;

/**
 * Scanner for JavaScript test files.
 */
public class JavascriptTestScanner {

    private final Class<?> testClass;
    private final JavascriptTestInterpreter interpreter;

    public JavascriptTestScanner(Class<?> testClass, JavascriptTestInterpreter interpreter) {
        this.testClass = testClass;
        this.interpreter = interpreter;
    }
    
    public void parseTests(TestScannerListener listener) {
        try {
            for (File f : scanForTests()) {
                String data = FileUtils.fileRead(f, "UTF-8");
                listener.fileScanned("file://" + f.getAbsolutePath(), data, interpreter.listTestsFrom(data));
            }
        } catch (IOException e) {
            throw new JavascriptInterpreterException("Error occured while reading Javascript test file", e);
        }
    }
    
    private Iterable<File> scanForTests() {
        List<File> foundFiles = new ArrayList<>();
        for (String included : findMatchingIncludedFilePaths(ClassUtils.findClassAnnotation(PhantomConfiguration.class, testClass, true))) {
            foundFiles.add(new File(included));
        }
        return foundFiles;
    }
    
    private String[] findMatchingIncludedFilePaths(PhantomConfiguration config) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(config.tests());
        
        StringBuilder path = new StringBuilder(System.getProperty("user.dir"));
        if (!System.getProperty("user.dir").endsWith(File.separator)) {
            path.append(File.separator);
        }
        path.append(config.testsBaseDir());
        
        scanner.setBasedir(path.toString());
        scanner.scan();    
        
        String[] included = scanner.getIncludedFiles();
        String[] finalFilePaths = new String[included.length];
        for (int i = 0; i < finalFilePaths.length; i++) {
            StringBuilder finalPath = new StringBuilder();
            
            if (!config.testsBaseDir().isEmpty()) {
                finalPath.append(config.testsBaseDir());
                
                if (!config.testsBaseDir().endsWith(File.separator)) {
                    finalPath.append(File.separator);
                }
            }
            finalPath.append(included[i]);
            
            finalFilePaths[i] = finalPath.toString();
        }
        return finalFilePaths;
    }
    

}
