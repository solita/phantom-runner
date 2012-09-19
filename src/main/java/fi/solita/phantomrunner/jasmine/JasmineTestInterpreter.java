package fi.solita.phantomrunner.jasmine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.jetty.PhantomWebSocketHandler;
import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.util.JavascriptBlockUtils;

public class JasmineTestInterpreter extends AbstractJavascriptTestInterpreter {

   private final PhantomWebSocketHandler handler;
   private final ResourceLoader resLoader = new DefaultResourceLoader();

   public JasmineTestInterpreter(Class<?> testClass) {
      this(new String[]{"classpath:jasmine/jasmine.js"}, testClass);
   }
   
   public JasmineTestInterpreter(String[] libPaths, Class<?> testClass) {
      super(libPaths, testClass);
      
      handler = new PhantomWebSocketHandler();
   }

   @Override
   public String getRunnerPath() {
      try {
            return resLoader.getResource("jasmine/jasmine-phantom-runner.js").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
   }

   @Override
   public PhantomWebSocketHandler getHandler() {
      return handler;
   }
   
   @Override
   public String getTestHTML(String[] additionalLibraries, String testDataUrl) {
       try {
           InputStream baseHtml = resLoader.getResource("jasmine/jasmine-test-runner.html").getInputStream();
           Document doc = Jsoup.parse(baseHtml, null, "");
           
           for (String lib : getLibPaths()) {
               appendScript(doc, lib);
           }
           
           for (String lib : additionalLibraries) {
               appendScript(doc, lib);
           }
           
           appendScript(doc, testDataUrl);
           
           return doc.outerHtml();
       } catch (IOException ioe) {
           throw new RuntimeException(ioe);
       }
   }
      
   @Override
   protected List<JavascriptTest> createTestsFrom(String data, Class<?> testClass) {
      List<JavascriptTest> tests = new ArrayList<>();
      
      for (String describe : JavascriptBlockUtils.findBlocks(data, "describe")) {
         if (!describe.isEmpty()) {
            tests.add(new JasmineSuite(describe, testClass));
         }
      }
      
      return tests;
   }

   @Override
   public boolean evaluateResult(JsonNode resultTree) {
      return resultTree.get("passed").asBoolean();
   }

   private void appendScript(Document doc, String libPath) throws IOException {
        doc.head().appendElement("script")
            .attr("type", "text/javascript")
            .attr("src", resLoader.getResource(libPath).getFile().getAbsolutePath());
    }
}
