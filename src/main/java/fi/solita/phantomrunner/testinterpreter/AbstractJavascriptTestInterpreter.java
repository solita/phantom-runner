package fi.solita.phantomrunner.testinterpreter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractJavascriptTestInterpreter implements JavascriptTestInterpreter {

	private final String[] libPaths;
	
	public AbstractJavascriptTestInterpreter(String[] libPaths) {
		this.libPaths = libPaths;
	}

	@Override
	public List<JavascriptTest> listTestsFrom(File jsFile) {
		try {
			return createTestsFrom(FileUtils.fileRead(jsFile, "UTF-8"));
		} catch (IOException e) {
			throw new JavascriptInterpreterException("Error occured while reading Javascript test file", e);
		}
	}
	
	@Override
	public String[] getLibPaths() {
		return libPaths;
	}
	
	protected abstract List<String> testFunctionNames();
	protected abstract List<JavascriptTest> createTestsFrom(String line);
}
