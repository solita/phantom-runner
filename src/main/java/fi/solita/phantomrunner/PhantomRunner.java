package fi.solita.phantomrunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import fi.solita.phantomrunner.testinterpreter.JavascriptInterpreterException;
import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.MasterJavascriptTest;
import fi.solita.phantomrunner.util.ClassUtils;
import fi.solita.phantomrunner.util.FileUtils;

public class PhantomRunner extends Suite {

    private final PhantomProcess process;
    private final PhantomServer server;
    
    private final MasterJavascriptTest master;
    private final PhantomProcessNotifier processNotifier;
    
    public PhantomRunner(Class<?> klass) throws Exception {
        // whoopee, no Collections.emptyList() or ImmutableList due to Java generics
        super(klass, new LinkedList<Runner>());
            
        JavascriptTestInterpreter interpreter = createInterpreter();
        
        PhantomConfiguration config = findPhantomConfigAnnotation();
                
        this.server = new PhantomServerFactory(config, interpreter).build().start();
        this.processNotifier = this.server.createNotifier();
        this.master = new MasterJavascriptTest(getTestClass().getJavaClass(), interpreter, config.injectLibs());
        this.process = new PhantomProcess(config, 
                phantomRunnerInitializerPath(),
                server.getServerScriptPath(),
                interpreter.getRunnerPath(),
                utilsPath());
    }
    
    @Override
    public Description getDescription() {
        return master.asDescription(getTestClass().getJavaClass());
    }

    @Override
    public void run(RunNotifier notifier) {
        // Maven doesn't necessarily call getDescription so we need to invoke it in here to ensure
        // all descriptions are cached properly
        getDescription();
        
        master.run(notifier, processNotifier);
        
        try {
            process.stop();
            server.stop();
        } catch (Exception e) {
            // ignore since these exceptions are irrelevant
        }
    }
        
    private JavascriptTestInterpreter createInterpreter() {
        JavascriptTestInterpreterConfiguration interpreterConfig = findPhantomConfigAnnotation().interpreter();
        Class<? extends JavascriptTestInterpreter> interpreterClass = interpreterConfig.interpreterClass();
        
        try {
            if (interpreterConfig.libraryFilePaths()[0].length() > 0) {
                return interpreterClass
                        .getConstructor(String[].class, Class.class)
                        .newInstance((Object) interpreterConfig.libraryFilePaths(), getTestClass().getClass());
            } else {
                return interpreterClass
                        .getConstructor(Class.class)
                        .newInstance(getTestClass().getClass());
            }

        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new JavascriptInterpreterException("Couldn't create interpreter", e);
        }
    }

    private PhantomConfiguration findPhantomConfigAnnotation() {
        return ClassUtils.findClassAnnotation(PhantomConfiguration.class, getTestClass().getJavaClass(), true);
    }
    
    private String phantomRunnerInitializerPath() {
        try {
            Resource initializer = new DefaultResourceLoader().getResource("classpath:PhantomRunnerInitializer.js");
            return FileUtils.extractResourceToTempDirectory(initializer, "", true).getAbsolutePath();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private String utilsPath() {
        try {
            Resource initializer = new DefaultResourceLoader().getResource("classpath:utils.js");
            return FileUtils.extractResourceToTempDirectory(initializer, "", true).getAbsolutePath();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
