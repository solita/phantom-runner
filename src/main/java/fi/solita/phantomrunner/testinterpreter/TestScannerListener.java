package fi.solita.phantomrunner.testinterpreter;

import java.util.List;

public interface TestScannerListener {

	void fileScanned(String data, List<JavascriptTest> testsFromData);

}
