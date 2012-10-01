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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Adds a synchronous API for communicating with PhantomJs vai websockets. Allows calling 
 * {@link #send(String, PhantomMessageListener)} in such a way that the call doesn't return before the
 * tests are ran at PhantomJs and it has sent the response data.
 */
public class PhantomRunnerWebSocket implements WebSocket.OnTextMessage {

    private final AtomicReference<Connection> connection = new AtomicReference<>();
    private final List<PhantomMessageListener> listeners = new CopyOnWriteArrayList<>();
    private final Object socketLock = new Object();
    
    PhantomRunnerWebSocket() {
    }
    
    @Override
    public void onOpen(Connection connection) {
        this.connection.set(connection);
        
        synchronized (socketLock) {
            socketLock.notifyAll();
        }
    }

    @Override
    public void onClose(int closeCode, String message) {
        // nothing to do...
    }

    @Override
    public void onMessage(String data) {
        try {
            for (PhantomMessageListener listener : listeners) {
                listener.message(new ObjectMapper().readTree(data));
            }
            listeners.clear();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String msg, PhantomMessageListener responseListener) throws IOException {
        try {
            listeners.add(responseListener);
            synchronized (socketLock) {
                if (connection.get() == null) {
                    socketLock.wait(1000);
                }
            }
            connection.get().sendMessage(msg);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    public boolean isOpen() {
        return connection.get().isOpen();
    }

    public void close() {
        connection.get().close();
        connection.set(null);
    }
}
