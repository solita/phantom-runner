package fi.solita.phantomrunner.jasmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.util.JavascriptBlockUtils;
import fi.solita.phantomrunner.util.Strings;

public class JasmineTestInterpreter extends AbstractJavascriptTestInterpreter {

	public JasmineTestInterpreter(String[] libPaths) {
		super(libPaths);
	}

	@Override
	public String getRunnerPath() {
		return new File(getClass().getClassLoader().getResource("jasmine/jasmine-phantom-runner.js").getFile()).getAbsolutePath();
	}

	@Override
	protected List<JavascriptTest> createTestsFrom(String data) {
		List<JavascriptTest> tests = new ArrayList<>();
		
		for (String describe : JavascriptBlockUtils.findBlocks(data, "describe")) {
			if (!describe.isEmpty()) {
				tests.add(new JasmineSuite(describe));
			}
		}
		
		return tests;
	}

	public static class JasmineSuite implements JavascriptTest {

		private final String name;
		private final String data;
		
		private final List<JasmineSpec> specs = new ArrayList<>();

		public JasmineSuite(String describe) {
			this.name = Strings.firstMatch(describe, "(?<=\").*(?=\")");
			this.data = describe;

			// AST would be so much better than this string pseudo parsing but there just isn't reasonable
			// lightweight javascript parsers available. Tried Rhino and ANTLR with proper grammar to no
			// avail. Google Caja might be a solution but there's no maven artifacts available :(
			for (String spec : JavascriptBlockUtils.findBlocks(describe, "it(")) {
				if (!spec.isEmpty()) {
					specs.add(new JasmineSpec(spec, this));
				}
			}
			
		}

		@Override
		public String getTestName() {
			return name;
		}

		@Override
		public String getTestData() {
			return data;
		}

		@Override
		public boolean isSuite() {
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder("Jasmine Test Suite:\n");
			builder.append("  Suite data:\n");
			builder.append(Strings.indentLines(data, 4));
			builder.append("\n  Specifications:\n");
			
			for (int i = 0; i < specs.size(); i++) {
				if (i > 0) {
					builder.append(",\n");
				}
				builder.append(Strings.indentLines(specs.get(i).toString(), 4));
			}
			return builder.toString();
		}
	}
	
	public static class JasmineSpec implements JavascriptTest {

		private final String name;
		private final String testData;
		private final JasmineSuite parentSuite;
		
		public JasmineSpec(String testData, JasmineSuite parent) {
			this.name = Strings.firstMatch(testData, "(?<=\").*(?=\")");
			this.parentSuite = parent;
			this.testData = testData;
		}
		
		@Override
		public String getTestName() {
			return name;
		}

		@Override
		public String getTestData() {
			// we want to provide the whole suite data since we cannot run specs alone without their
			// suites due to javascript variable scoping and it's usage
			return parentSuite.getTestData();
		}
		
		@Override
		public boolean isSuite() {
			return false;
		}
		
		@Override
		public String toString() {
			return testData;
		}
	}
}
