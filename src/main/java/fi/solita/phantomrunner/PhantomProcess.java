package fi.solita.phantomrunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.AssertionFailedError;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.solita.phantomrunner.stream.StreamPiper;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;
import fi.solita.phantomrunner.util.Strings;

public class PhantomProcess {

	private final String path;
	private final JavascriptTestInterpreter interpreter;
	
	private final Process phantomProcess;
	
	private final Map<String, String> resourceCache = new HashMap<>();
	
	public PhantomProcess(PhantomConfiguration config, JavascriptTestInterpreter interpreter) {
		this.path = config.phantomPath();
		this.interpreter = interpreter;
		
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

	public void initializeTestRun(String testFileData, String[] libPaths, String[] extLibs) {
		try {
			Map<String, Object> postData = new HashMap<>();
			postData.put("testFileData", testFileData);
			postData.put("libDatas", convertToResourceStrings(libPaths));
			postData.put("extLibs", extLibs.length == 0 || extLibs[0].length() == 0 ? "" : convertToResourceStrings(extLibs));
			
			post("init", new ObjectMapper().writer().writeValueAsString(postData));
		} catch (IOException e) {
			throw new PhantomProcessException(e);
		}
	}
	
	private String[] convertToResourceStrings(String[] libPaths) {
		try {
			// it is typical that this method will be called multiple times with same libPaths during a test run,
			// thus we'll cache the data since there's no point in loading that same data over and over again
			// from the disk
			String[] result = new String[libPaths.length];
			ResourceLoader loader = new DefaultResourceLoader();
			for (int i = 0; i < libPaths.length; i++) {
				String data = resourceCache.get(libPaths[i]);
				if (data == null) {
					data = Strings.streamToString(loader.getResource(libPaths[i]).getInputStream());
					resourceCache.put(libPaths[i], data);
				}
				result[i] = data;
			}
			return result;
		} catch (IOException ioe) {
			throw new PhantomProcessException(ioe);
		}
	}

	public void runTest(JavascriptTest javascriptTest) {
		if (!evaluateResult(
				post("run", javascriptTest.isTest() 
						? javascriptTest.getSuiteName() + "#!#" + javascriptTest.getTestName()
						: javascriptTest.getTestName()))) {
			throw new AssertionFailedError();
		}
	}

	private boolean evaluateResult(InputStream responseData) {
		try {
			return interpreter.evaluateResult(new ObjectMapper().readTree(responseData));
		} catch (IOException e) {
			throw new PhantomProcessException(e);
		}
	}

	private InputStream post(String urlFragment, String postData) {
		try {
			
			HttpClient httpclient = new DefaultHttpClient();
			
			HttpEntity entity = new StringEntity(postData, "UTF-8");
			
			HttpPost post = new HttpPost("http://localhost:18080/" + urlFragment);
			post.setEntity(entity);
			HttpResponse response = httpclient.execute(post);
			return response.getEntity().getContent();
		} catch (ClientProtocolException cpe) {
			throw new PhantomProcessException(cpe);
		} catch (IOException e) {
			throw new PhantomProcessException(e);
		}
	}
	

	
}
