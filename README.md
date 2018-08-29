Phantom Runner
==============

What it is?
-----------

Phantom Runner is a custom JUnit runner which allows user to run [Jasmine](https://jasmine.github.io/) based JavaScript tests in the [PhantomJS](http://phantomjs.org/) headless browser. This allows a seamless integration to anything that can run JUnit tests: your favourite Java IDE (Eclipse, Netbeans, IntelliJ etc), your favourite build tool (Ant, Maven, Gradle...) or your favourite continuous integration server (Jenkins, Bamboo, Continuum...).

# We're at alpha! The API and functionality may change at any time. Use at your own risk.

Why?
----

One of the main problems in JavaScript development is the fact that JavaScript code is hard to test. Code must be executed in a browser since non-brower implementations of JavaScript interpreters don't have access to DOM which is essential in browser handling. Because of this most of the JavaScript testing is done inside a browser by using specific test runner pages which execute the written tests and show the results directly in the browser.

This is a workflow problem. Developer writes his/hers code in one application, then changes the view to a browser, refreshes that browser and then visualizes the changes. In the ideal world these tests should be executable directly in the editor. This is what Phantom Runner tries to achieve.

Usage
-----

In order to use Phantom Runner you need three things:

1. Install latest version of [PhantomJS](http://phantomjs.org/)
2. Create a separate JUnit class and configure it to use PhantomRunner
3. Write some [Jasmine](http://pivotal.github.com/jasmine/) tests

After this you can execute the created JUnit test class in your favourite environment, for example in Eclipse you can use the normal right click -> run as -> JUnit test to run your Jasmine tests.

### Configuring PhantomRunner JUnit runner

An example configuration for PhantomRunner would be:

```java
@RunWith(PhantomRunner.class)
@PhantomConfiguration(
		phantomPath="phantomjs", 
		tests="**/jasmine-tests/*-test.js",
		injectLibs={"classpath:lib/require.js", "classpath:require-js-config.js"},
		interpreter=@JavascriptTestInterpreterConfiguration(
				interpreterClass=JasmineTestInterpreter.class
		),
		server=@PhantomServerConfiguration(
				serverClass=PhantomJettyServer.class,
				port=18080
		))
public class ExamplePhantomRunnerTest {
}
```

Let's go through the @PhantomConfiguration annotation settings and what they do:

- **phantomPath**: defines the path where the PhantomJS executable can be found. Usually it is a good idea to add it to your operating system's path so you won't cause a system specific dependency into your PhantomRunner configurations.
- **tests**: a ant-style path pattern which is used to scan for the Jasmine tests. This is done relative to the JVM execution path, thus usually it is the root of your project
- **injectLibs**: a string array of resource paths of JavaScript files you want to inject into the execution path so that your tests will see those. In the example require.js module system and it's configuration file is injected. Now all tests can use the require.js to require dependencies before executing tests. For more information about require.js please see it's [documentation](http://requirejs.org/docs/api.html)
- **interpreter**: defines the JavaScript test interpreter to be used. Currently only JasmineTestInterpreter is available but in the future for example QUnit may be supported.
- **server**: defines the used server implementation which works as the binding point between Java and JavaScript. Currently available are PhantomJettyServer and PhantomJsServer (see below)

### Server implementations

Phantom Runner provides two different server implementations which allow the Java code to talk with the JavaScript code. Currently two choices are available:

- **PhantomJettyServer**: Launches an embedded [Jetty](http://www.eclipse.org/jetty/) and ties PhantomJs with it through websockets.
- **PhantomJsServer**: Launches the PhantomJS embedded web server and Java code sends HTTP requests to this server telling it what to do. **Please note that currently the port configuration does *not* affect PhantomJsServer!**

Basically these server implementations are used to first initialize a specific test run (meaning, one Jasmine test file) and then to execute each of these tests one at a time. This allows a more refined integration with JUnit which can provide execution times and so on since the control of running each test is at Java side. This architectural design will allow (in the future, not in 0.1) also integration with real server implementations which would allow you to run full system level tests with Phantom Runner using PhantomJS.

PhantomJsServer starts up a lot faster than PhantomJettyServer which makes it ideal for small test suites. When your test suites get bigger however then PhantomJettyServer will be the faster one since it's websocket based communication is way faster than common HTTP requests and responses.

Good to know
------------

Since Phantom Runner controls an external process which it runs few things need to be remembered:

- PhantomJS process can get "stuck", this means that if you forcefully terminate the test execution run (kill it, not send sigterm) then JVM doesn't get a chance to kill the PhantomJS process. Sometime this might be necessary to do: for example if your code ends up in an infinite loop. If this happens then please remember to kill the PhantomJS process manually via your operating system's process management ("kill" in *nix, task manager in Windows).
- Please be certain that proper execution rights are set so that Phantom Runner can actually start the PhantomJS process
- Phantom Runner cannot completely replace your browser based test execution. It is effectively using only one browser - PhantomJS - and there's a whole bunch of other browsers out there. You should create system level tests with more traditional ways (eg. [Selenium](http://seleniumhq.org/)) which will execute your code in the real browsers. Most of the bugs should be catched by your Phantom Runner/Jasmine tests - let your system level tests catch the browser differences
