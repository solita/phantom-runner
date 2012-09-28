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

    private final File serverScriptFile;

    public PhantomJsServer(JavascriptTestInterpreter interpreter, PhantomServerConfiguration serverConfig) {
        // TODO: how can we send the server port for PhantomJS from here?
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
