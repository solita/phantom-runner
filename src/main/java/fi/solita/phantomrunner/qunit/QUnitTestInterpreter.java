package fi.solita.phantomrunner.qunit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

public class QUnitTestInterpreter extends AbstractJavascriptTestInterpreter {

	private static final String TEST = "test";
	private static final String ASYNC = "asyncTest";
	
	public QUnitTestInterpreter(String[] libPaths) {
		super(libPaths);
	}

	@Override
	public String getRunnerPath() {
		return new File(getClass().getClassLoader().getResource("phantomjs/phantom-runner.js").getFile()).getAbsolutePath();
	}
	
	@Override
	protected List<String> testFunctionNames() {
		return Arrays.asList(TEST, ASYNC, "QUnit." + TEST, "QUnit." + ASYNC);
	}

	@Override
	protected List<JavascriptTest> createTestsFrom(String data) {
		// FIXME: this String based parsing is ugly, a proper AST implementation would be 100x better.
		//        Sadly though for now I couldn't find a proper Javascript parser for Java. Rhino has
		//        one but it really isn't meant to be used by outside code (at least that's how it feels
		//        like) and Google Caja has really really poor Maven support. Most likely I'll go with
		//        Caja in the near future but for now I'll go with String parsing so that the rest of
		//        the framework can be finished.
		List<JavascriptTest> tests = new ArrayList<>();
		for (String testDeclaration : data.split("(?=(test|asyncTest)\\()")) {
			if (!StringUtils.isBlank(testDeclaration)) {
				tests.add(new QUnitTest(testDeclaration));
			}
		}
		return tests;
	}
	
	public static class QUnitTest implements JavascriptTest {

		private static final Pattern TEST_NAME_PATTERN = Pattern.compile("(test|asyncTest)*\\(\"([a-zA-ZäöÄÖåÅ 0-9]*)\".*", Pattern.DOTALL);
		
		private final String name;
		private final String data;
		
		public QUnitTest(String data) {
			
			Matcher matcher = TEST_NAME_PATTERN.matcher(data);
			
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Not a proper QUnit test");
			}
			
			this.name = matcher.group(2);
			this.data = data;
		}
	
		@Override
		public String getTestName() {
			return name;
		}

		@Override
		public String getTestData() {
			return data;
		}
	}

}
