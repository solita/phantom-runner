package fi.solita.phantomrunner.testinterpreter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractJavascriptTestInterpreter implements JavascriptTestInterpreter {

	public AbstractJavascriptTestInterpreter(String[] libPaths) {
		
	}

	@Override
	public List<JavascriptTest> listTestsFrom(File jsFile) {
		try {
			List<JavascriptTest> tests = new ArrayList<>();
			List<String> testNames = testFunctionNames();
			// FIXME: this String based parsing is ugly, a proper AST implementation would be 100x better.
			//        Sadly though for now I couldn't find a proper Javascript parser for Java. Rhino has
			//        one but it really isn't meant to be used by outside code (at least that's how it feels
			//        like) and Google Caja has really really poor Maven support. Most likely I'll go with
			//        Caja in the near future but for now I'll go with String parsing so that the rest of
			//        the framework can be finished.
			String[] lines = FileUtils.fileRead(jsFile, "UTF-8").split("\\n");
			for (String line : lines) {
				for (String testName : testNames) {
					if (line.startsWith(testName)) {
						tests.add(createTestFrom(line));
					}
				}
			}
			
			return tests;
		} catch (IOException e) {
			throw new JavascriptInterpreterException("Error occured while reading Javascript test file", e);
		}
	}
	
	protected abstract List<String> testFunctionNames();
	protected abstract JavascriptTest createTestFrom(String line);
}
