package fi.solita.phantomrunner.jetty;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.WebSocket;

import com.fasterxml.jackson.databind.ObjectMapper;

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
