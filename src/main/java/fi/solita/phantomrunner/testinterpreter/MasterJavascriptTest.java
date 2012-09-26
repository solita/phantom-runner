package fi.solita.phantomrunner.testinterpreter;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import fi.collin.util.collections.UnmodifiableLinkedReferencingList;
import fi.solita.phantomrunner.PhantomProcessNotifier;

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
            
            for (JavascriptTest test : new UnmodifiableLinkedReferencingList<>(testLists)) {
                cache.addChild(test.asDescription(testClass));
            }
        }
        return cache;
    }  

    @Override
    public void run(RunNotifier notifier, PhantomProcessNotifier processNotifier) {
        for (JavascriptTestFile testFile : testFiles) {
            
            processNotifier.initializeTestRun(
                    interpreter.getTestHTML(extLibs, testFile.getFilePath()), 
                    interpreter.getLibPaths(), 
                    extLibs);
            
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
        
        private final List<JavascriptTestFile> testFiles = new ArrayList<>();
        
        @Override
        public void fileScanned(String filePath, String fileData, List<JavascriptTest> testsFromData) {
            this.testFiles.add(new JavascriptTestFile(filePath, fileData, testsFromData));
        }
        
        public List<JavascriptTestFile> getTests() {
            return testFiles;
        }
    }

}
