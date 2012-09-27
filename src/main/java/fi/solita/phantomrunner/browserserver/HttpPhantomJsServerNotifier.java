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
