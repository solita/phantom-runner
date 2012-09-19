package fi.solita.phantomrunner.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;

import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomJettyServer {

	private static final Log log = LogFactory.getLog(PhantomJettyServer.class);
	private final Server server;
	private final PhantomWebSocketHandler interpreterHandler;
	
	public PhantomJettyServer(JavascriptTestInterpreter interpreter, String resourceBase) {
		// we don't want to see the Jetty logging, disable it
		org.eclipse.jetty.util.log.Log.setLog(new DiscardingJettyLogger());
		
		this.server = new Server(18080);
		
		this.interpreterHandler = interpreter.getHandler();
		this.interpreterHandler.setHandler(createResourceHandler(resourceBase));
		this.server.setHandler(interpreterHandler);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					PhantomJettyServer.this.stop();
				} catch (Exception e) {
					// oh dear, something evil happened. At this point we are just about to stop the
					// execution of the JVM and thus we really cannot do anything anymore. Just write the
					// exception to log and be sad :(
					log.error("An exception occured while stopping JettyServer", e);
				}
			}
		});
	}
	
	private Handler createResourceHandler(String resourceBase) {
		ResourceHandler handler = new ResourceHandler();
		handler.setDirectoriesListed(false);
		handler.setResourceBase("file://" + resourceBase);
		return handler;
	}

	public PhantomJettyServer start() throws Exception {
		server.start();
		return this;
	}
	
	public PhantomJettyServer stop() throws Exception {
		if (server.isRunning()) {
			server.stop();
		}
		return this;
	}
	
	public PhantomProcessNotifier createNotifier() {
		return new PhantomProcessNotifier(interpreterHandler);
	}

}
