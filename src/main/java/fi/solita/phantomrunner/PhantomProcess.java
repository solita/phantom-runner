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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fi.solita.phantomrunner.stream.StreamPiper;
import fi.solita.phantomrunner.util.Strings;

public class PhantomProcess {

    private final String path;
    private final Process phantomProcess;
    
    public PhantomProcess(PhantomConfiguration config, String...jsFilePaths) {
        this.path = config.phantomPath();
        
        try {
            this.phantomProcess = new ProcessBuilder(Strings.stringList(path, jsFilePaths)).start();
            
            ExecutorService pool = Executors.newFixedThreadPool(2);
            
            final Future<?> inFuture = pool.submit(new StreamPiper(this.phantomProcess.getInputStream(), System.out));
            final Future<?> errFuture = pool.submit(new StreamPiper(this.phantomProcess.getErrorStream(), System.err));
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    inFuture.cancel(true);
                    errFuture.cancel(true);
                    phantomProcess.destroy();
                }
            });
            
        } catch (IOException e) {
            throw new PhantomProcessException("Couldn't start PhantomJS process, check your configuration", e);
        }
    }
    
    public void stop() {
        this.phantomProcess.destroy();
    }

}
