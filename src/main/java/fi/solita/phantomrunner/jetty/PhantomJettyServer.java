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
import fi.solita.phantomrunner.PhantomServer;
import fi.solita.phantomrunner.PhantomServerConfiguration;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;
import fi.solita.phantomrunner.util.FileUtils;

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
