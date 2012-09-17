package fi.solita.phantomrunner.jetty;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;

public class PhantomWebSocketHandler extends WebSocketHandler {

	private final AtomicReference<PhantomRunnerWebSocket> socket = new AtomicReference<>();
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
			final AtomicReference<JsonNode> result = new AtomicReference<>();
			
			synchronized (socketLock) {
				if (socket.get() == null) {
					socketLock.wait(1000);
				}
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
