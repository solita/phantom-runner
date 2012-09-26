package fi.solita.phantomrunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fi.solita.phantomrunner.stream.StreamPiper;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomProcess {

	private final String path;
	private final Process phantomProcess;
	
	public PhantomProcess(PhantomConfiguration config, JavascriptTestInterpreter interpreter) {
		this.path = config.phantomPath();
		
		try {
			this.phantomProcess = new ProcessBuilder(
									Arrays.asList(path, interpreter.getRunnerPath()))
								.start();
			
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
