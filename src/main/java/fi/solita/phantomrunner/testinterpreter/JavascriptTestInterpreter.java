package fi.solita.phantomrunner.testinterpreter;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.jetty.PhantomWebSocketHandler;

public interface JavascriptTestInterpreter {

	List<JavascriptTest> listTestsFrom(String data);

	String[] getLibPaths();

	String getRunnerPath();

	boolean evaluateResult(JsonNode resultTree);

	PhantomWebSocketHandler getHandler();

}
