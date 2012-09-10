package fi.solita.phantomrunner;

import org.junit.runner.RunWith;

import fi.solita.phantomrunner.qunit.QUnitTestInterpreter;

@RunWith(PhantomRunner.class)
@PhantomConfiguration(
		phantomPath="phantomjs", 
		tests="**/*-test.js",
		interpreter=@JavascriptTestInterpreterConfiguration(
				interpreterClass=QUnitTestInterpreter.class,
				libraryFilePaths="**/target/**/qunit.js"
		))
public class PhantomRunnerTest {

}
