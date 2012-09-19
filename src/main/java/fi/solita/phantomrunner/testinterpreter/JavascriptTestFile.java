package fi.solita.phantomrunner.testinterpreter;

import java.util.List;

public class JavascriptTestFile {

    private final String filePath;
    private final String fileData;
    private final List<JavascriptTest> tests;

    public JavascriptTestFile(String filePath, String fileData, List<JavascriptTest> tests) {
        this.filePath = filePath;
        this.fileData = fileData;
        this.tests = tests;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileData() {
        return fileData;
    }

    public List<JavascriptTest> getTests() {
        return tests;
    }

}
