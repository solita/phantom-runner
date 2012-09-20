package fi.solita.phantomrunner.jetty;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

public class PhantomProcessNotifier {

	private final ObjectMapper mapper = new ObjectMapper();
	private final PhantomWebSocketHandler handler;
	
	public PhantomProcessNotifier(PhantomWebSocketHandler interpreterHandler) {
		this.handler = interpreterHandler;
	}

	public void initializeTestRun(String testFileData, String[] libPaths, String[] extLibs) {
		ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();

		send(json
				.put("type", "init")
				.put("testFileData", testFileData)
			.build());
	}

	public JsonNode runTest(JavascriptTest javascriptTest) {
		ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();
		
		String suiteAndTestName = javascriptTest.isTest() 
				? javascriptTest.getSuiteName() + "#!#" + javascriptTest.getTestName()
				: javascriptTest.getTestName();
		
		return send(json
				.put("type", "run")
				.put("testName", suiteAndTestName)
			.build());
	}

	private JsonNode send(Map<String, Object> json) {
		try {
			return handler.sendMessageToConnections(mapper.writeValueAsString(json));
		} catch (IOException e) {
			// TODO: some nicer exception type please...
			throw new RuntimeException(e);
		}
	}
}
