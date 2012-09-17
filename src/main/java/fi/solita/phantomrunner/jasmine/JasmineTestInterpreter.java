package fi.solita.phantomrunner.jasmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.jetty.PhantomWebSocketHandler;
import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.util.JavascriptBlockUtils;

public class JasmineTestInterpreter extends AbstractJavascriptTestInterpreter {

	private final PhantomWebSocketHandler handler;

	public JasmineTestInterpreter(Class<?> testClass) {
		this(new String[]{"classpath:jasmine/jasmine.js"}, testClass);
	}
	
	public JasmineTestInterpreter(String[] libPaths, Class<?> testClass) {
		super(libPaths, testClass);
		
		handler = new PhantomWebSocketHandler();
	}

	@Override
	public String getRunnerPath() {
		return new File(getClass().getClassLoader().getResource("jasmine/jasmine-phantom-runner.js").getFile()).getAbsolutePath();
	}

	@Override
	public PhantomWebSocketHandler getHandler() {
		return handler;
	}
	
	@Override
	protected List<JavascriptTest> createTestsFrom(String data, Class<?> testClass) {
		List<JavascriptTest> tests = new ArrayList<>();
		
		for (String describe : JavascriptBlockUtils.findBlocks(data, "describe")) {
			if (!describe.isEmpty()) {
				tests.add(new JasmineSuite(describe, testClass));
			}
		}
		
		return tests;
	}

	@Override
	public boolean evaluateResult(JsonNode resultTree) {
		return resultTree.get("passed").asBoolean();
	}

}
