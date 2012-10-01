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
package fi.solita.phantomrunner.jetty;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.springframework.core.io.DefaultResourceLoader;

import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.PhantomRunner;
import fi.solita.phantomrunner.PhantomServer;
import fi.solita.phantomrunner.PhantomServerConfiguration;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;
import fi.solita.phantomrunner.util.FileUtils;

/**
 * Jetty based {@link PhantomServer} implementation. Uses websockets in communicating with PhantomJs process.
 * Define the port Jetty should use in {@link PhantomServerConfiguration#port()} when configuring the
 * {@link PhantomRunner}.
 */
public class PhantomJettyServer implements PhantomServer {

    private static final Log log = LogFactory.getLog(PhantomJettyServer.class);
    private final Server server;
    private final PhantomWebSocketHandler websocketHandler;
    private final File serverScriptFile;
    
    public PhantomJettyServer(JavascriptTestInterpreter interpreter, PhantomServerConfiguration serverConfig) {
        // we don't want to see the Jetty logging, disable it
        org.eclipse.jetty.util.log.Log.setLog(new DiscardingJettyLogger());
        
        this.server = new Server(serverConfig.port());
        
        this.websocketHandler = new PhantomWebSocketHandler();
        this.websocketHandler.setHandler(createResourceHandler(serverConfig.resourceBase()));
        this.server.setHandler(websocketHandler);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    PhantomJettyServer.this.stop();
                } catch (Exception e) {
                    // oh dear, something evil happened. At this point we are just about to stop the
                    // execution of the JVM and thus we really cannot do anything anymore. Just write the
                    // exception to log and be sad :(
                    log.error("An exception occured while stopping JettyServer", e);
                }
            }
        });
        
        try {
            this.serverScriptFile = FileUtils.extractResourceToTempDirectory(
                    new DefaultResourceLoader().getResource("classpath:jetty-server/jetty-server.js"), 
                    "jetty-server", 
                    true);
        } catch (IOException ioe) {
            throw new RuntimeException("jetty-server.js couldn't be loaded", ioe);
        }
    }

   @Override
    public PhantomServer start() throws Exception {
        server.start();
        return this;
    }
    
    @Override
    public PhantomServer stop() throws Exception {
        if (server.isRunning()) {
            server.stop();
        }
        return this;
    }
    
    @Override
    public PhantomProcessNotifier createNotifier() {
        return new WebsocketPhantomNotifier(websocketHandler);
    }
    
    @Override
    public String getServerScriptPath() {
        return serverScriptFile.getAbsolutePath();
    }
    
    private Handler createResourceHandler(String resourceBase) {
        ResourceHandler handler = new ResourceHandler();
        handler.setDirectoriesListed(false);
        handler.setResourceBase("file://" + resourceBase);
        return handler;
    }

}
