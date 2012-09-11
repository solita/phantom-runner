package fi.solita.phantomrunner.jasmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;

import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
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
		for (String describe : data.split("(?=describe)")) {
			if (!describe.isEmpty()) {
				tests.add(new JasmineSuite(describe));
			}
		}
		
		return tests;
	}

	public static class JasmineSuite {

		private final String name;

		public JasmineSuite(String describe) {
			this.name = Strings.firstMatch(describe, "(?<=\").*(?=\")");
			// TODO: need to implement JasmineSpec parsing and figure out the mechanism how this all is
			//       tied into JUnit. Most likely JasmineSuite will be a JUnit suite (doh) and specs are
			//       tests. This brings up a problem though: JasmineSpec tests cannot be ran separately
			//       from the JasmineSuite due to the way Jasmine works (variable scoping etc). Thus in a
			//       way it would be more appropriate to use JasmineSuite objects as JUnit tests but this
			//       would fail with IDE hierarchy view... Make a compromise with this - most likely
			//       it is something in between where JasmineSpecs are shown in the tree and the result
			//       JSON received from PhantomJS will contain the test results for that specific suite.
			//       After this all tests are triggered as ran at the same time for JUnit. This has the
			//       disadvantage of not showing proper execution times per test but the execution time for
			//       the whole suite. Maybe Jasmine can tell us the execution times and we can embed that
			//       into the response AND to JUnit?
		}
		
	}
	
	public static class JasmineSpec implements JavascriptTest {

		private final String name;
		private final List<JasmineSpec> children = new ArrayList<>();
		
		public JasmineSpec(String testData) {
			this.name = Strings.firstMatch(testData, "(?<=\").*(?=\")");
			
			if (testData.startsWith("describe")) {
				
			}
		}
		
		@Override
		public String getTestName() {
			return name;
		}

		@Override
		public String getTestData() {
			
			return null;
		}
		
		@Override
		public boolean isSuite() {
			return !children.isEmpty();
		}
	}
}
