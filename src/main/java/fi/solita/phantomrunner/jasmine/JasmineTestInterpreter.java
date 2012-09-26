package fi.solita.phantomrunner.jasmine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.io.Files;

import fi.solita.phantomrunner.jetty.PhantomWebSocketHandler;
import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.util.JavascriptBlockUtils;
import fi.solita.phantomrunner.util.Strings;

public class JasmineTestInterpreter extends AbstractJavascriptTestInterpreter {

    private final PhantomWebSocketHandler handler;
    private final ResourceLoader resLoader = new DefaultResourceLoader();
    private final File runnerFile;

    public JasmineTestInterpreter(Class<?> testClass) {
        this(new String[] { "classpath:test-fw/jasmine/jasmine.js" }, testClass);
    }

    public JasmineTestInterpreter(String[] libPaths, Class<?> testClass) {
        super(libPaths, testClass);

        handler = new PhantomWebSocketHandler();
        
        this.runnerFile = extractRunnerIntoTempDirectory();
    }

    @Override
    public String getRunnerPath() {
        return runnerFile.getAbsolutePath();
    }

    @Override
    public PhantomWebSocketHandler getHandler() {
        return handler;
    }

    @Override
    public String getTestHTML(String[] additionalLibraries, String testDataUrl) {
        try {
            InputStream baseHtml = resLoader.getResource(
                    "jasmine/jasmine-test-runner.html").getInputStream();
            Document doc = Jsoup.parse(baseHtml, null, "");
            doc.outputSettings().escapeMode(EscapeMode.none);

            // Interpreter libraries usually come from inside a jar-package and thus they cannot be 
            // referenced via file system URI. Because of this we'll inject the scripts themselves 
            // directly to the HTML.
            for (String lib : getLibPaths()) {
                injectScript(doc, lib);
            }

            for (String lib : additionalLibraries) {
                appendScript(doc, lib);
            }

            appendScript(doc, testDataUrl);

            return doc.html();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    protected List<JavascriptTest> createTestsFrom(String data,
            Class<?> testClass) {
        List<JavascriptTest> tests = new ArrayList<>();

        for (String describe : JavascriptBlockUtils
                .findBlocks(data, "describe")) {
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
        doc.head()
            .appendElement("script")
                .attr("type", "text/javascript")
                .attr("src", resLoader.getResource(libPath).getFile().getAbsolutePath());
    }

    private void injectScript(Document doc, String libPath) throws IOException {
        doc.head()
            .appendElement("script")
                .attr("type", "text/javascript")
                .text(Strings.streamToString(resLoader.getResource(libPath).getInputStream()));
    }
    
    /**
     * This extraction is done because runner file is inside a jar package and thus it is impossible to
     * reference from PhantomJS. We cannot also pass it's content for PhantomJS since PhantomJS expects
     * to get a file URI reference as an command line argument which it will use to command itself. Thus
     * we'll extract the file into current user's temporary directory and reference it from there.
     */
    private File extractRunnerIntoTempDirectory() {
        try {
            final File tempDir = Files.createTempDir();
            tempDir.deleteOnExit();
            
            final File jasmineDir = new File(tempDir, "jasmine");
            jasmineDir.mkdir();
            jasmineDir.deleteOnExit();
            
            final File runnerFile = new File(jasmineDir, "jasmine-phantom-runner.js");
            runnerFile.createNewFile();
            runnerFile.deleteOnExit();
            
            Resource res = resLoader.getResource("jasmine/jasmine-phantom-runner.js");

            IOUtil.copy(res.getInputStream(), new FileWriter(runnerFile), "UTF-8");
            
            return runnerFile;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
