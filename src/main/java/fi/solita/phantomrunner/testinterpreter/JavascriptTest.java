package fi.solita.phantomrunner.testinterpreter;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import fi.solita.phantomrunner.PhantomProcessNotifier;

public interface JavascriptTest {

    String getTestName();

    String getTestData();
    
    Description asDescription(Class<?> parentTestClass);

    void run(RunNotifier notifier, PhantomProcessNotifier processNotifier);

    boolean isTest();

    String getSuiteName();
}
