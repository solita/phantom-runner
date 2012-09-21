package fi.solita.phantomrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;


public class PhantomRunnerTest {
    
    @Test
    public void testRunnerWithJasmineTests() {
        Result result = new JUnitCore().run(PhantomRunnerTestHolder.class);
        
        // 6 == PhantomRunnerTestHolder + 5 Jasmine specs
        assertEquals(6, result.getRunCount());
        assertEquals(2, result.getFailureCount());
        assertFalse(result.wasSuccessful());
    }
}
