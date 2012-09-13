package fi.solita.phantomrunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.plexus.util.DirectoryScanner;

import fi.solita.phantomrunner.stream.StreamPiper;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomProcess {

	private final String path;
	private final JavascriptTestInterpreter interpreter;
	
	private final Process phantomProcess;
	
	public PhantomProcess(PhantomConfiguration config, JavascriptTestInterpreter interpreter) {
		this.path = config.phantomPath();
		this.interpreter = interpreter;
		
		try {
			this.phantomProcess = new ProcessBuilder(
					asList(
							path, 
							interpreter.getRunnerPath(), 
							convertToAbsolute(interpreter.getLibPaths())
						)
					).start();
			
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

	public void initializeTestRun(String testFileData) {
		post("init", testFileData);
	}
	
	public void runTest(JavascriptTest javascriptTest) {
		post("run", javascriptTest.getTestName());
	}

	private InputStream post(String urlFragment, String postData) {
		try {
			
			HttpClient httpclient = new DefaultHttpClient();
			
			HttpEntity entity = new StringEntity(postData, "UTF-8");
			
			HttpPost post = new HttpPost("http://localhost:18080/" + urlFragment);
			post.setEntity(entity);
			HttpResponse response = httpclient.execute(post);
			new StreamPiper(response.getEntity().getContent(), System.out).run();
			return response.getEntity().getContent();
		} catch (ClientProtocolException cpe) {
			throw new PhantomProcessException(cpe);
		} catch (IOException e) {
			throw new PhantomProcessException(e);
		}
	}
	
	private String[] convertToAbsolute(String[] libPaths) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setIncludes(libPaths);
		scanner.setBasedir(System.getProperty("user.dir"));
		scanner.scan();
		
		String[] scanned = scanner.getIncludedFiles();
		for (int i = 0; i < scanned.length; i++) {
			scanned[i] = new File(scanned[i]).getAbsolutePath();
		}
		return scanned;
	}
	
	private List<String> asList(String first, String second, String[] rest) {
		List<String> result = new ArrayList<>();
		result.add(first);
		result.add(second);
		for (String cmd: rest) {
			result.add(cmd);
		}
		return result;
	}
}
