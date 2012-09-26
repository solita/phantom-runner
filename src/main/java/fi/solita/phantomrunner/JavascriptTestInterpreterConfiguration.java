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

    /**
     * Interpreter class to be used for the test execution. This class with it's resource files provide
     * {@link PhantomRunner} way to execute and interpret JavaScript tests and binds them to JUnit test framework.
     * All implementations of {@link JavascriptTestInterpreter} provide a version of the actual testing library
     * JavaScript file but if the version used is old you may override the library file(s) with
     * {@link JavascriptTestInterpreterConfiguration#libraryFilePaths()}. Please note though that this <b>may</b>
     * break the interpreter if the semantics of the library have changed a lot.
     */
    Class<? extends JavascriptTestInterpreter> interpreterClass();
    
    /**
     * <p>Path where the used JavaScript test library should be looked for. By default all {@link JavascriptTestInterpreter}
     * instances know where to get their libraries but in case of old library you may override this by providing a
     * another library path here. Please note though that the Java code doesn't change and thus something may brake
     * if another version of the library is provided.</p>
     * 
     * <p>Library paths follow the conventional Java resource loading mechanism with the additional support for
     * <i>classpath</i> protocol. Thus all of the following are valid paths:</p>
     * 
     * <ul>
     *   <li>file://./some/path/foo.js</li>
     *   <li>classpath:some/path/foo.js</li>
     *   <li>http://foobar.com/some/path/foo.js</li>
     * </ul>
     * 
     * <p>All loaded resources are cached for the execution of {@link PhantomRunner}. This prevents both excessive
     * disc I/O and also network I/O in case of external network resources.</p>
     */
    String[] libraryFilePaths() default "";
    
}
