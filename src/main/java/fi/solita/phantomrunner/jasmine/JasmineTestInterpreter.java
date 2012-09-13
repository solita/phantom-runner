package fi.solita.phantomrunner.jasmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.util.JavascriptBlockUtils;

public class JasmineTestInterpreter extends AbstractJavascriptTestInterpreter {

	public JasmineTestInterpreter(String[] libPaths, Class<?> testClass) {
		super(libPaths, testClass);
	}

	@Override
	public String getRunnerPath() {
		return new File(getClass().getClassLoader().getResource("jasmine/jasmine-phantom-runner.js").getFile()).getAbsolutePath();
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

}
