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

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

/**
 * Notifier implementation which sends messages to PhantomJS via websocket.
 */
public class WebsocketPhantomNotifier implements PhantomProcessNotifier {
    
    private final ObjectMapper mapper = new ObjectMapper();
    private final PhantomWebSocketHandler handler;
    
    public WebsocketPhantomNotifier(PhantomWebSocketHandler interpreterHandler) {
        this.handler = interpreterHandler;
    }

    @Override
    public void initializeTestRun(String testFileData) {
        ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();
        
        send(json
                .put("type", "init")
                .put("testFileData", testFileData)
            .build());
    }

    @Override
    public JsonNode runTest(JavascriptTest javascriptTest) {
        ImmutableMap.Builder<String, Object> json = ImmutableMap.builder();
        
        String suiteAndTestName = javascriptTest.isTest() 
                ? javascriptTest.getSuiteName() + "#!#" + javascriptTest.getTestName()
                : javascriptTest.getTestName();
        
        return send(json
                .put("type", "run")
                .put("testName", suiteAndTestName)
            .build());
    }

    private JsonNode send(Map<String, Object> json) {
        try {
            return handler.sendMessageToConnections(mapper.writeValueAsString(json));
        } catch (IOException e) {
            // TODO: some nicer exception type please...
            throw new RuntimeException(e);
        }
    }
}
