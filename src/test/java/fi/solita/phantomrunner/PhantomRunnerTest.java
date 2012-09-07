package fi.solita.phantomrunner;

import org.junit.runner.RunWith;

@RunWith(PhantomRunner.class)
@PhantomConfiguration(phantomPath="phantomjs", tests="**/*-test.js")
public class PhantomRunnerTest {

}
