package fi.solita.phantomrunner.testinterpreter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import fi.solita.phantomrunner.PhantomConfiguration;
import fi.solita.phantomrunner.util.ClassUtils;

public class JavascriptTestScanner {

	private final Class<?> testClass;
	private final JavascriptTestInterpreter interpreter;

	public JavascriptTestScanner(Class<?> testClass, JavascriptTestInterpreter interpreter) {
		this.testClass = testClass;
		this.interpreter = interpreter;
	}
	
	public void parseTests(TestScannerListener listener) {
		try {
			for (File f : scanForTests()) {
				String data = FileUtils.fileRead(f, "UTF-8");
				listener.fileScanned("file://" + f.getAbsolutePath(), data, interpreter.listTestsFrom(data));
			}
		} catch (IOException e) {
			throw new JavascriptInterpreterException("Error occured while reading Javascript test file", e);
		}
	}
	
	private Iterable<File> scanForTests() {
		List<File> foundFiles = new ArrayList<>();
		for (String included : findMatchingIncludedFilePaths(ClassUtils.findClassAnnotation(PhantomConfiguration.class, testClass, true))) {
			foundFiles.add(new File(included));
		}
		return foundFiles;
	}
	
	private String[] findMatchingIncludedFilePaths(PhantomConfiguration config) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(config.tests());
		
		StringBuilder path = new StringBuilder(System.getProperty("user.dir"));
		if (!System.getProperty("user.dir").endsWith(File.separator)) {
			path.append(File.separator);
		}
		path.append(config.testsBaseDir());
		
		scanner.setBasedir(path.toString());
		scanner.scan();	
		
		String[] included = scanner.getIncludedFiles();
		String[] finalFilePaths = new String[included.length];
		for (int i = 0; i < finalFilePaths.length; i++) {
			StringBuilder finalPath = new StringBuilder();
			
			if (!config.testsBaseDir().isEmpty()) {
				finalPath.append(config.testsBaseDir());
				
				if (!config.testsBaseDir().endsWith(File.separator)) {
					finalPath.append(File.separator);
				}
			}
			finalPath.append(included[i]);
			
			finalFilePaths[i] = finalPath.toString();
		}
		return finalFilePaths;
	}
	

}
