package fi.solita.phantomrunner;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class PhantomRunnerTest {
    
    @Test
    public void testRunnerWithJasmineTests() {
        JUnitCore core = new JUnitCore();
        
        final List<String> failedTestNames = new ArrayList<>();
        
        core.addListener(new RunListener() {
            @Override
            public void testFailure(Failure failure) throws Exception {
                String header = failure.getTestHeader();
                failedTestNames.add(header.substring(0, header.indexOf('(')));
            } 
        });
        
        Result result = core.run(PhantomRunnerTestHolder.class);
        
        // 6 == PhantomRunnerTestHolder + 5 Jasmine specs
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
