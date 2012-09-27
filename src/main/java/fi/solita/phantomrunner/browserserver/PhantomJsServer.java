package fi.solita.phantomrunner.browserserver;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.DefaultResourceLoader;

import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.PhantomServer;
import fi.solita.phantomrunner.PhantomServerConfiguration;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;
import fi.solita.phantomrunner.util.FileUtils;

public class PhantomJsServer implements PhantomServer {

    private final JavascriptTestInterpreter interpreter;
    private final PhantomServerConfiguration config;
    private final File serverScriptFile;

    public PhantomJsServer(JavascriptTestInterpreter interpreter, PhantomServerConfiguration serverConfig) {
        this.interpreter = interpreter;
        this.config = serverConfig;
        
        try {
            this.serverScriptFile = FileUtils.extractResourceToTempDirectory(
                    new DefaultResourceLoader().getResource("classpath:phantomjs-server/phantomjs-server.js"), 
                    "phantomjs-server", 
                    true);
        } catch (IOException ioe) {
            throw new RuntimeException("phantomjs-server.js couldn't be loaded", ioe);
        }
    }
    
    @Override
    public PhantomServer start() throws Exception {
        // nothing to do, server starts automatically when the server script is loaded (has to, no other way)
        return this;
    }

    @Override
    public PhantomServer stop() throws Exception {
        // TODO: maybe we should send the stop signal for PhantomJS? Not strictly necessary since it will die
        //       when the process dies
        return this;
    }

    @Override
    public PhantomProcessNotifier createNotifier() {
        return new HttpPhantomJsServerNotifier();
    }

    @Override
    public String getServerScriptPath() {
        return serverScriptFile.getAbsolutePath();
    }

}
