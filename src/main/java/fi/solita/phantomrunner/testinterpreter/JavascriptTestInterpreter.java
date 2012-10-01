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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.PhantomRunner;

/**
 * JavaScript test framework integration interface. All supported test frameworks must implement this
 * interface to provide proper integration with {@link PhantomRunner}.
 */
public interface JavascriptTestInterpreter {

    /**
     * Provides a test file data as String to the interpreter for parsing. This is the main entry point
     * for parsing the tests and the returned tests should be interpreter specific implementations.
     */
    List<JavascriptTest> listTestsFrom(String data);

    /**
     * Returns a HTML file which will be used as the "test runner" for this interpreter.
     * 
     * @param additionalJsFilePaths An array of resource paths which should be added to the page 
     * before the test file
     * @param testFilePath A resource path to the JavaScript test file to be embedded to the test runner page
     */
    String getTestHTML(String[] additionalJsFilePaths, String testFilePath);
    
    
    /**
     * Returns a resource path array for all JavaScript library files needed for by the JavaScript test
     * framework this interpreter represents.
     */
    String[] getLibPaths();
    
    /**
     * Returns the path to the JavaScript runner file for this interpreter. This runner file is the real
     * glue between PhantomJS and the testing framework. It should call the testing framework API and do
     * the dirty work needed for the actual tests to run. It is also responsible of actually evaluating the
     * data from the tests etc.
     */
    String getRunnerPath();

    /**
     * Evaluate the given JSON result data received from the JavaScript runner. Did the executed test pass or
     * not?
     */
    boolean evaluateResult(JsonNode resultTree);

}
