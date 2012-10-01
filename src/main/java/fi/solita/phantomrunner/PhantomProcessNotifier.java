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
package fi.solita.phantomrunner;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

/**
 * <p>Notifier which informs the PhantomJs process of two things:</p>
 * 
 * <ul>
 *   <li>Test file is about to be executed (an HTML file containing the test script)</li>
 *   <li>A test described in the above test file is about to be executed</li>
 * </ul>
 */
public interface PhantomProcessNotifier {

    /**
     * Initializes a JavaScript test file in the PhantomJs for execution. Always call this before
     * calling {@link #runTest(JavascriptTest)} to ensure PhantomJs is aware of the tests.
     * 
     * @param testFileData An HTML file containing the actual test data as inline script
     */
    void initializeTestRun(String testFileData);

    /**
     * Executes the given test at PhantomJs.
     * 
     * @param javascriptTest
     * @return JsonNode containing the result JSON generated at JavaScript code
     */
    JsonNode runTest(JavascriptTest javascriptTest);

}
