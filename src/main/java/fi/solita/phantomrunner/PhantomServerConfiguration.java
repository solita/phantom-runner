package fi.solita.phantomrunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PhantomServerConfiguration {

    Class<? extends PhantomServer> serverClass();
    String resourceBase() default "file://./src/main/resources";
    
}
