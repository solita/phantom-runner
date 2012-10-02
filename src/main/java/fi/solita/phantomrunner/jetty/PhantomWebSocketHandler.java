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

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;

public class PhantomWebSocketHandler extends WebSocketHandler {

    private final AtomicReference<PhantomRunnerWebSocket> socket = new AtomicReference<PhantomRunnerWebSocket>();
    private final Object socketLock = new Object();
    
    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        if (this.socket.get() != null) {
            throw new IllegalStateException("WebSocket connection already opened");
        }
        PhantomRunnerWebSocket socket = new PhantomRunnerWebSocket();
        this.socket.set(socket);
        
        synchronized (socketLock) {
            socketLock.notifyAll();
        }
        
        return socket;
    }

    public JsonNode sendMessageToConnections(String msg) {
        try {
            final Object requestLock = new Object();
            final AtomicReference<JsonNode> result = new AtomicReference<JsonNode>();
            
            synchronized (socketLock) {
                if (socket.get() == null) {
                    // wait for 10 secs for the socket to open
                    socketLock.wait(TimeUnit.SECONDS.toMillis(10));
                }
            }
            
            if (socket.get() == null) {
                throw new PhantomWebSocketException("No open websocket connection available, cannot send message");
            }
    
            try {
                socket.get().send(msg, new PhantomMessageListener() {
                    @Override
                    public void message(JsonNode readTree) {
                        synchronized (requestLock) {
                            result.set(readTree);
                            requestLock.notifyAll();
                        }
                    }
                });
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
    
            synchronized (requestLock) {    
                requestLock.wait(10000);
            }
            
            return result.get();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }
}
