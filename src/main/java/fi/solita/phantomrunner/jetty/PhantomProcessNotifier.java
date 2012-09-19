package fi.solita.phantomrunner.jetty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import fi.solita.phantomrunner.PhantomProcessException;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

public class PhantomProcessNotifier {

	private final ObjectMapper mapper = new ObjectMapper();
	private final PhantomWebSocketHandler handler;
	private final Map<String, String> resourceCache = new HashMap<>();
	
	public PhantomProcessNotifier(PhantomWebSocketHandler interpreterHandler) {
		this.handler = interpreterHandler;
	}

	public void initializeTestRun(String testFileData, String[] libPaths, String[] extLibs) {
		ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();

		send(json
				.put("type", "init")
				.put("testFileData", testFileData)
				.put("libDatas", convertToResourceStrings(libPaths))
				.put("extLibs", extLibs.length == 0 || extLibs[0].length() == 0 ? "" : convertToResourceStrings(extLibs))
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
	
	private String[] convertToResourceStrings(String[] libPaths) {
		try {
			// it is typical that this method will be called multiple times with same libPaths during a test run,
			// thus we'll cache the data since there's no point in loading that same data over and over again
			// from the disk
			String[] result = new String[libPaths.length];
			ResourceLoader loader = new DefaultResourceLoader();
			for (int i = 0; i < libPaths.length; i++) {
				String data = resourceCache.get(libPaths[i]);
				if (data == null) {
					data = loader.getResource(libPaths[i]).getFile().getAbsolutePath();
					resourceCache.put(libPaths[i], data);
				}
				result[i] = data;
			}
			return result;
		} catch (IOException ioe) {
			throw new PhantomProcessException(ioe);
		}
	}
}
