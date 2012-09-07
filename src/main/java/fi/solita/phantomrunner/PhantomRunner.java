package fi.solita.phantomrunner;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

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
		PhantomConfiguration config = findClassAnnotation(PhantomConfiguration.class, true);
		
		Iterable<File> jsTestFiles = scanForTests(config);
		for (File f : jsTestFiles) {
			System.out.println("Found: " + f);
		}
		return null;
	}

	private Iterable<File> scanForTests(PhantomConfiguration config) {
		List<File> foundFiles = new ArrayList<>();
		for (String included : findMatchingIncludedFilePaths(config)) {
			foundFiles.add(new File(included));
		}
		return foundFiles;
	}

	private String[] findMatchingIncludedFilePaths(PhantomConfiguration config) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(config.tests());
		scanner.setBasedir(config.testsBaseDir());
		scanner.scan();	
		return scanner.getIncludedFiles();
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
