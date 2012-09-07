package fi.solita.phantomrunner;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import fi.solita.phantomrunner.testinterpreter.JavascriptInterpreterException;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomRunner extends Suite {

	public PhantomRunner(Class<?> klass) throws InitializationError {
		// whoopee, no Collections.emptyList() or ImmutableList due to Java generics
		super(klass, new LinkedList<Runner>());
	}

	@Override
	public Description getDescription() {
		Description master = Description.createSuiteDescription(getTestClass().getJavaClass());
		for (Description child : buildJavascriptTestDescriptions()) {
			master.addChild(child);
		}
		return master;
	}

	@Override
	public void run(RunNotifier notifier) {
		
	}

	private Iterable<Description> buildJavascriptTestDescriptions() {
		List<Description> descriptions = new ArrayList<>();
		for (File f : scanForTests()) {
			Description desc = Description.createSuiteDescription(f.getName());
			
			for (Description jsTestFunction : parseJsTestFunctions(f)) {
				desc.addChild(jsTestFunction);
			}
			
			descriptions.add(desc);
		}
		return descriptions;
	}

	private Iterable<File> scanForTests() {
		List<File> foundFiles = new ArrayList<>();
		for (String included : findMatchingIncludedFilePaths(findClassAnnotation(PhantomConfiguration.class, true))) {
			foundFiles.add(new File(included));
		}
		return foundFiles;
	}
	
	private Iterable<Description> parseJsTestFunctions(File jsFile) {
		JavascriptTestInterpreter interpreter = createInterpreter();
		
		return Iterables.transform(interpreter.listTestsFrom(jsFile), new Function<JavascriptTest, Description>() {
			@Override
			public Description apply(JavascriptTest input) {
				return Description.createSuiteDescription(input.getTestName());
			}
		});
	}

	private JavascriptTestInterpreter createInterpreter() {
		JavascriptTestInterpreterConfiguration interpreterConfig = findClassAnnotation(PhantomConfiguration.class, true).interpreter();
		Class<? extends JavascriptTestInterpreter> interpreterClass = interpreterConfig.interpreterClass();
		
		try {
			return interpreterClass
				.getConstructor(String[].class)
				.newInstance((Object) interpreterConfig.libraryFilePaths());
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new JavascriptInterpreterException("Couldn't create interpreter", e);
		}
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
	
	@SuppressWarnings("unchecked")
	private <T extends Annotation> T findClassAnnotation(Class<T> clazz, boolean required) {
		for (Annotation a : getTestClass().getAnnotations()) {
			if (a.annotationType().equals(clazz)) {
				return (T) a;
			}
		}
		
		if (required) {
			throw new IllegalStateException(String.format(
					"Illegal PhantomRunner configuration, no %s annotation found at %s type level", 
					clazz.getName(), getTestClass().getJavaClass().getName()));
		}
		return null;
	}
}
