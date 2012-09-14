package fi.solita.phantomrunner;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import fi.solita.phantomrunner.testinterpreter.JavascriptInterpreterException;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.MasterJavascriptTest;
import fi.solita.phantomrunner.util.ClassUtils;

public class PhantomRunner extends Suite {

	private final MasterJavascriptTest master;
	
	private final PhantomProcess process;
	
	public PhantomRunner(Class<?> klass) throws InitializationError {
		// whoopee, no Collections.emptyList() or ImmutableList due to Java generics
		super(klass, new LinkedList<Runner>());
		
		JavascriptTestInterpreter interpreter = createInterpreter();
		this.master = new MasterJavascriptTest(getTestClass().getJavaClass(), interpreter, findPhantomConfigAnnotation().injectLibs());
		this.process = new PhantomProcess(findPhantomConfigAnnotation(), interpreter);
	}

	@Override
	public Description getDescription() {
		return master.asDescription(getTestClass().getJavaClass());
	}

	@Override
	public void run(RunNotifier notifier) {
		master.run(notifier, process);
	}
		
	private JavascriptTestInterpreter createInterpreter() {
		JavascriptTestInterpreterConfiguration interpreterConfig = findPhantomConfigAnnotation().interpreter();
		Class<? extends JavascriptTestInterpreter> interpreterClass = interpreterConfig.interpreterClass();
		
		try {
			if (interpreterConfig.libraryFilePaths()[0].length() > 0) {
				return interpreterClass
						.getConstructor(String[].class, Class.class)
						.newInstance((Object) interpreterConfig.libraryFilePaths(), getTestClass().getClass());
			} else {
				return interpreterClass
						.getConstructor(Class.class)
						.newInstance(getTestClass().getClass());
			}

		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new JavascriptInterpreterException("Couldn't create interpreter", e);
		}
	}

	private PhantomConfiguration findPhantomConfigAnnotation() {
		return ClassUtils.findClassAnnotation(PhantomConfiguration.class, getTestClass().getJavaClass(), true);
	}
}
