package fi.solita.phantomrunner;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

public interface PhantomProcessNotifier {

    void initializeTestRun(String testFileData, String[] libPaths, String[] extLibs);

    JsonNode runTest(JavascriptTest javascriptTest);

}
