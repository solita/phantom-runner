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

import fi.solita.phantomrunner.browserserver.PhantomJsServer;
import fi.solita.phantomrunner.jetty.PhantomJettyServer;

/**
 * <p>Configuration annotation for {@link PhantomRunner}. In order to run JavaScript tests you first need to
 * tell the {@link PhantomRunner} what kind of testing framework should be used for executing your tests and
 * where these tests can be found.</p> 
 * 
 * <p>Also you can configure external JavaScript libraries or code files to be injected before the tests are 
 * ran. Thus if you wish you can inject your application code like this or use this feature to inject a proper
 * module loading mechanism such as <a href="http://requirejs.org/">Require.js</a> and do the application code
 * loading with that in your tests.</p>
 * 
 * <p>In addition you can change the {@link PhantomServer} implementation used for communicating between the
 * {@link PhantomRunner} and PhantomJs process. For more information please see the implementation classes
 * and {@link PhantomServerConfiguration}.
 */
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
     */
    String testsBaseDir() default "src/test/js";
    
    /**
     * <p>A list of JavaScript file paths which should be injected to the test pages before any tests are 
     * actually ran. This is the preferred way to provide your application an external module loading 
     * mechanism (such as the excellent <a href="http://requirejs.org/">RequireJs</a>) which you can then 
     * easily use to load your actual application code which you are going to test.</p>
     * 
     * <p>You can also use this mechanism to inject your application code JavaScripts directly if you wish so
     * that they are available for your tests before they are executed.</p>
     * 
     * <p>File paths follow the conventional Java resource loading mechanism with the additional support for
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
    String[] injectJavascriptFiles() default "";
    
    /**
     * Interpreter type to be used when running the tests.
     */
    JavascriptTestInterpreterConfiguration interpreter();

    /**
     * Server to be used in the test execution. Currently two implementations are available:
     * {@link PhantomJettyServer} and {@link PhantomJsServer}.
     */
    PhantomServerConfiguration server() default 
        @PhantomServerConfiguration(
            serverClass=PhantomJettyServer.class
        );
}
