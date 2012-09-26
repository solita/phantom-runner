package fi.solita.phantomrunner.browserserver;

import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.PhantomServer;
import fi.solita.phantomrunner.PhantomServerConfiguration;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomJsServer implements PhantomServer {

    private final JavascriptTestInterpreter interpreter;
    private final PhantomServerConfiguration config;

    public PhantomJsServer(JavascriptTestInterpreter interpreter, PhantomServerConfiguration serverConfig) {
        this.interpreter = interpreter;
        this.config = serverConfig;
    }
    
    @Override
    public PhantomServer start() throws Exception {
        return this;
    }

    @Override
    public PhantomServer stop() throws Exception {
        return null;
    }

    @Override
    public PhantomProcessNotifier createNotifier() {
        return null;
    }

}
