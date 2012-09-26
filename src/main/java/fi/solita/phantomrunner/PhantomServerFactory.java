package fi.solita.phantomrunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import fi.solita.phantomrunner.testinterpreter.JavascriptTestInterpreter;

public class PhantomServerFactory {

    private final PhantomServerConfiguration config;
    private final JavascriptTestInterpreter interpreter;
    
    public PhantomServerFactory(PhantomConfiguration config, JavascriptTestInterpreter interpreter) {
        this.config = config.server();
        this.interpreter = interpreter;
    }
    
    public PhantomServer build() {
        try {
            Constructor<? extends PhantomServer> ctr = config.serverClass()
                    .getConstructor(JavascriptTestInterpreter.class, PhantomServerConfiguration.class);
            return ctr.newInstance(interpreter, config);
            
        } catch (NoSuchMethodException nsme) { 
            throw new IllegalArgumentException(String.format(
                    "Illegal server class of type %s provided, provided class must have a constructor of " +
                    "signature %s(%s, %s)",
                    config.serverClass(),
                    config.serverClass(),
                    JavascriptTestInterpreter.class,
                    PhantomServerConfiguration.class));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
