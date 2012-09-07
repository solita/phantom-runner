package fi.solita.phantomrunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface JavascriptTestInterpreterConfiguration {

	Class<? extends JavascriptTestInterpreter> interpreterClass();
	String[] libraryFilePaths();
	
}
