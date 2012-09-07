package fi.solita.phantomrunner.qunit;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

public class QUnitTestInterpreter extends AbstractJavascriptTestInterpreter {

	private static final String TEST = "test";
	private static final String ASYNC = "asyncTest";
	
	public QUnitTestInterpreter(String[] libPaths) {
		super(libPaths);
	}

	@Override
	protected List<String> testFunctionNames() {
		return Arrays.asList(TEST, ASYNC, "QUnit." + TEST, "QUnit." + ASYNC);
	}

	@Override
	protected JavascriptTest createTestFrom(String line) {
		return new QUnitTest(line);
	}
	
	public static class QUnitTest implements JavascriptTest {

		private static final Pattern TEST_NAME_PATTERN = Pattern.compile("(test|asyncTest)*\\(\"([a-zA-ZäöÄÖåÅ 0-9]*)\",.*", Pattern.DOTALL);
		
		private final String name;
		
		public QUnitTest(String line) {
			Matcher matcher = TEST_NAME_PATTERN.matcher(line);
			
			if (!matcher.matches()) {
				throw new IllegalArgumentException("Not a proper QUnit test line");
			}
			
			this.name = matcher.group(2);
		}
	
		@Override
		public String getTestName() {
			return name;
		}
	}
}
