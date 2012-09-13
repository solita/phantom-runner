package fi.solita.phantomrunner.testinterpreter;

import java.util.List;

public interface JavascriptTestInterpreter {

	List<JavascriptTest> listTestsFrom(String data);

	String[] getLibPaths();

	String getRunnerPath();

}
