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
package fi.solita.phantomrunner.jetty;

import org.eclipse.jetty.util.log.Logger;

/**
 * A Jetty logger which does exactly nothing. Can be used to silence Jetty logger completely.
 */
public class DiscardingJettyLogger implements Logger {

    @Override
    public String getName() {
        return "DiscardingLogger";
    }

    @Override
    public void warn(String msg, Object... args) {}

    @Override
    public void warn(Throwable thrown) {}

    @Override
    public void warn(String msg, Throwable thrown) {}

    @Override
    public void info(String msg, Object... args) {}

    @Override
    public void info(Throwable thrown) {}

    @Override
    public void info(String msg, Throwable thrown) {}

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {}

    @Override
    public void debug(String msg, Object... args) {}

    @Override
    public void debug(Throwable thrown) {}

    @Override
    public void debug(String msg, Throwable thrown) {}

    @Override
    public Logger getLogger(String name) {
        return this;
    }

    @Override
    public void ignore(Throwable ignored) {}

}
