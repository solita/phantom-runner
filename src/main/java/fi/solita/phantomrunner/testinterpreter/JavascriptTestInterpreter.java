package fi.solita.phantomrunner.testinterpreter;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface JavascriptTestInterpreter {

	List<JavascriptTest> listTestsFrom(String data);

	String[] getLibPaths();

	String getRunnerPath();

	boolean evaluateResult(JsonNode resultTree);

}
