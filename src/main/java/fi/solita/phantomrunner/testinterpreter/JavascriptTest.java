package fi.solita.phantomrunner.testinterpreter;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import fi.solita.phantomrunner.PhantomProcess;

public interface JavascriptTest {

	String getTestName();

	String getTestData();
	
	Description asDescription(Class<?> parentTestClass);

	void run(RunNotifier notifier, PhantomProcess process);
}
