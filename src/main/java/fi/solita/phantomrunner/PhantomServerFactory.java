/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Solita Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
