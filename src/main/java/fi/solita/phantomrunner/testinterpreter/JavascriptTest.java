/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Solita Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fi.solita.phantomrunner.testinterpreter;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.PhantomRunner;

/**
 * Class representing a single or multiple JavaScript tests. All implementations of 
 * {@link JavascriptTestInterpreter} should use their own versions of this interface to provide the needed
 * information for {@link PhantomRunner} and JUnit for proper test execution.
 */
public interface JavascriptTest {

    /**
     * Returns the name of this test if available. Implementations should never return null.
     */
    String getTestName();

    /**
     * Returns the JavaScript test as String which this object represents.
     */
    String getTestData();
    
    /**
     * Converts this test to JUnit {@link Description} object. A class is provided as parameter to satisfy
     * {@link Description} constructor needs since with JavaScript tests there are no concrete test classes
     * to link into {@link Description}.
     */
    Description asDescription(Class<?> parentTestClass);

    /**
     * Executes this test. Provided {@link RunNotifier} should be used to report test execution results
     * for JUnit and {@link PhantomProcessNotifier} can be used to communicate with PhantomJS and trigger
     * the test execution in the browser.
     */
    void run(RunNotifier notifier, PhantomProcessNotifier processNotifier);

    /**
     * Is this a single test or a suite of multiple tests?
     */
    boolean isTest();

    /**
     * Returns the name of this test suite if available (that is, if {@link #isTest()} returns false).
     */
    String getSuiteName();
}
