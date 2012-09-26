package fi.solita.phantomrunner.testinterpreter;

import java.util.List;

public abstract class AbstractJavascriptTestInterpreter implements JavascriptTestInterpreter {

    private final String[] libPaths;
    private final Class<?> testClass;
    
    public AbstractJavascriptTestInterpreter(String[] libPaths, Class<?> testClass) {
        this.libPaths = libPaths;
        this.testClass = testClass;
    }

    @Override
    public List<JavascriptTest> listTestsFrom(String data) {
        return createTestsFrom(data, testClass);
    }
    
    @Override
    public String[] getLibPaths() {
        return libPaths;
    }
    
    protected abstract List<JavascriptTest> createTestsFrom(String data, Class<?> testClass);
}
