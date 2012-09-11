package fi.solita.phantomrunner;

import org.junit.runner.RunWith;

import fi.solita.phantomrunner.jasmine.JasmineTestInterpreter;

@RunWith(PhantomRunner.class)
@PhantomConfiguration(
		phantomPath="phantomjs", 
		tests="**/*-test.js",
		interpreter=@JavascriptTestInterpreterConfiguration(
				interpreterClass=JasmineTestInterpreter.class,
				libraryFilePaths="**/target/**/jasmine.js"
		))
public class PhantomRunnerTest {

}
