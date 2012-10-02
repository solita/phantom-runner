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

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import fi.collin.util.collections.UnmodifiableLinkedReferencingList;
import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.PhantomRunner;

/**
 * The core master test implementation. This is the "root" test created by {@link PhantomRunner} and it
 * handles all the child {@link JavascriptTest} object evaluations etc. MasterJavascriptTest is common for
 * all {@link JavascriptTestInterpreter} implementations and thus it is test framework independent.
 */
public final class MasterJavascriptTest implements JavascriptTest {

    private final Class<?> testClass;
    private final List<JavascriptTestFile> testFiles;
    private final JavascriptTestInterpreter interpreter;
    private final String[] extLibs;
    
    private Description cache;
    
    public MasterJavascriptTest(Class<?> testClass, JavascriptTestInterpreter interpreter, String[] extLibs) {
        this.testClass = testClass;
        this.interpreter = interpreter;
        this.extLibs = extLibs;
        
        MasterJavascriptListener listener = new MasterJavascriptListener();
        new JavascriptTestScanner(testClass, interpreter).parseTests(listener);
        this.testFiles = listener.getTests();
    }
    
    @Override
    public Description asDescription(Class<?> parentTestClass) {
        if (cache == null) {
            cache = Description.createSuiteDescription(testClass);
            
            // oh how I wait thee lambda expressions...
            Iterable<List<JavascriptTest>> testLists = Iterables.transform(testFiles, 
                    new Function<JavascriptTestFile, List<JavascriptTest>>() {
                @Override
                public List<JavascriptTest> apply(JavascriptTestFile input) {
                    return input.getTests();
                }
            });
            
            for (JavascriptTest test : new UnmodifiableLinkedReferencingList<JavascriptTest>(testLists)) {
                cache.addChild(test.asDescription(testClass));
            }
        }
        return cache;
    }  

    @Override
    public void run(RunNotifier notifier, PhantomProcessNotifier processNotifier) {
        for (JavascriptTestFile testFile : testFiles) {
            
            processNotifier.initializeTestRun(interpreter.getTestHTML(extLibs, testFile.getFilePath()));
            
            for (JavascriptTest test : testFile.getTests()) {
                test.run(notifier, processNotifier);
            }
        }
    }

    @Override
    public String getTestName() {
        return testClass.getName();
    }

    @Override
    public String getTestData() {
        return "";
    }
    
    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public String getSuiteName() {
        return "";
    }
    
    
    private static class MasterJavascriptListener implements TestScannerListener {
        
        private final List<JavascriptTestFile> testFiles = new ArrayList<JavascriptTestFile>();
        
        @Override
        public void fileScanned(String filePath, String fileData, List<JavascriptTest> testsFromData) {
            this.testFiles.add(new JavascriptTestFile(filePath, fileData, testsFromData));
        }
        
        public List<JavascriptTestFile> getTests() {
            return testFiles;
        }
    }

}
