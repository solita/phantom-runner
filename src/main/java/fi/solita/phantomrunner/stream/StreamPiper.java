package fi.solita.phantomrunner.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamPiper implements Runnable {

	private final InputStream in;
	private final OutputStream out;
	
	public StreamPiper(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}
	
	@Override
	public void run() {
		try {
			int read = -1;
			while ((read = in.read()) != -1) {
				out.write(read);
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
