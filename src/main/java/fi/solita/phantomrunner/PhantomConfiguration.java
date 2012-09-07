package fi.solita.phantomrunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PhantomConfiguration {

	/**
	 * Defines the absolute or relative path where PhantomJS executable is in the system. Preferred
	 * way is to set the PhantomJS executable in the system path so it can be referenced directly with
	 * simple "phantomjs" from anywhere. Default value is "phantomjs"
	 */
	String phantomPath() default "phantomjs";
	
	/**
	 * Ant-style path pattern list which is used to scan the Javascript tests to be ran.
	 */
	String[] tests();
	
	/**
	 * Base directory where the tests should be scanned relative to the working directory, default is src/test/js
	 * @return
	 */
	String testsBaseDir() default "src/test/js";
	
	/**
	 * Interpreter type to be used when running the tests.
	 */
	JavascriptTestInterpreterConfiguration interpreter();
}
