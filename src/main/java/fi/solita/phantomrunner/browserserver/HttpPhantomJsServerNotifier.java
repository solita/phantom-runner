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
package fi.solita.phantomrunner.browserserver;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import fi.solita.phantomrunner.PhantomProcessNotifier;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;

public class HttpPhantomJsServerNotifier implements PhantomProcessNotifier {

    private final ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public void initializeTestRun(String testFileData, String[] libPaths, String[] extLibs) {
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

    private JsonNode send(Map<String, Object> jsonMap) {
        try {
            HttpClient client = new HttpClient();
            
            // FIXME: should relly get this port from the JUnit configs but have no real way to pass this
            //        to JavaScript side of things yet, bummer
            PostMethod method = new PostMethod("http://localhost:18080");
            method.setRequestEntity(
                    new StringRequestEntity(mapper.writeValueAsString(jsonMap), "application/json", "UTF-8")
                );
            
            int responseCode = client.executeMethod(method);
            
            switch (responseCode) {
                case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                    throw new RuntimeException("Internal server error occured: " 
                                    + method.getResponseBodyAsString());
                default:
                    return mapper.readTree(method.getResponseBodyAsString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
