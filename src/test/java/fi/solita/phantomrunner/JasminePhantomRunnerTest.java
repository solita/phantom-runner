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
