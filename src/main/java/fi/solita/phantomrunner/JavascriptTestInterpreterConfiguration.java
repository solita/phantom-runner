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
