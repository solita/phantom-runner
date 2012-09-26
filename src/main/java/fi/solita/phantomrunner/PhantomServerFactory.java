package fi.solita.phantomrunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomServerFactory {

    private final PhantomConfiguration config;
    private final JavascriptTestInterpreter interpreter;
    
    public PhantomServerFactory(PhantomConfiguration config, JavascriptTestInterpreter interpreter) {
        this.config = config;
        this.interpreter = interpreter;
    }
    
    public PhantomServer build() {
        try {
            PhantomServerConfiguration serverConfig = config.server();
            
            Constructor<? extends PhantomServer> ctr = serverConfig.serverClass()
                    .getConstructor(JavascriptTestInterpreter.class, PhantomServerConfiguration.class);
            return ctr.newInstance(interpreter, serverConfig);
            
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
