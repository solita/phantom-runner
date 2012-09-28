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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import fi.solita.phantomrunner.browserserver.PhantomJsServer;
import fi.solita.phantomrunner.jasmine.JasmineTestInterpreter;
import fi.solita.phantomrunner.jetty.PhantomJettyServer;

public class JasminePhantomRunnerTest {
    
    // #######################
    // test 1
    
    @RunWith(PhantomRunner.class)
    @PhantomConfiguration(
            phantomPath="phantomjs", 
            tests="**/jasmine/*-test.js",
            interpreter=@JavascriptTestInterpreterConfiguration(
                    interpreterClass=JasmineTestInterpreter.class,
                    libraryFilePaths="classpath:test-fw/jasmine/jasmine.js"
            ),
            server=@PhantomServerConfiguration(
                    serverClass=PhantomJettyServer.class
                )
        )
    public static class JettyPhantomTestHolder {}
    
    @Test
    public void testRunnerWithJetty() {
        doTestingFor(JettyPhantomTestHolder.class);
    }
    
    
    // #######################
    // test 2
    
    @RunWith(PhantomRunner.class)
    @PhantomConfiguration(
            phantomPath="phantomjs", 
            tests="**/jasmine/*-test.js",
            interpreter=@JavascriptTestInterpreterConfiguration(
                    interpreterClass=JasmineTestInterpreter.class,
                    libraryFilePaths="classpath:test-fw/jasmine/jasmine.js"
            ),
            server=@PhantomServerConfiguration(
                    serverClass=PhantomJsServer.class
                ))
    public static class PhantomJsInternalServerTestHolder {}
    
    @Test
    public void testRunnerWithPhantomJsInternalServer() {
        doTestingFor(PhantomJsInternalServerTestHolder.class);
    }
    
    
    // #######################
    // actual testing method
    
    private void doTestingFor(Class<?> testClass) {
        JUnitCore core = new JUnitCore();
        
        final List<String> failedTestNames = new ArrayList<>();
        
        core.addListener(new RunListener() {
            @Override
            public void testFailure(Failure failure) throws Exception {
                String header = failure.getTestHeader();
                failedTestNames.add(header.substring(0, header.indexOf('(')));
            }
        });
        
        Result result = core.run(testClass);
        
        // 6 == holder test class + 5 Jasmine specs
        assertEquals(6, result.getRunCount());
        assertEquals(2, result.getFailureCount());
        
        // failure listener call count should equal to result.getFailureCount()
        assertEquals(result.getFailureCount(), failedTestNames.size());
        
        // specific tests should have failed
        assertTrue(failedTestNames.contains("and also it may fail too"));
        assertTrue(failedTestNames.contains("and some specifications which may fail"));
        
        assertFalse(result.wasSuccessful());
    }
}
