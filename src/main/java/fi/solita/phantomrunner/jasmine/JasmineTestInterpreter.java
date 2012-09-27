package fi.solita.phantomrunner.jasmine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.databind.JsonNode;

import fi.solita.phantomrunner.testinterpreter.AbstractJavascriptTestInterpreter;
import fi.solita.phantomrunner.testinterpreter.JavascriptTest;
import fi.solita.phantomrunner.util.FileUtils;
import fi.solita.phantomrunner.util.JavascriptBlockUtils;
import fi.solita.phantomrunner.util.Strings;

public class JasmineTestInterpreter extends AbstractJavascriptTestInterpreter {

    private static final String JASMINE_PATH_PREFIX = "test-fw/jasmine/";
    
    private final ResourceLoader resLoader = new DefaultResourceLoader();
    private final File runnerFile;

    public JasmineTestInterpreter(Class<?> testClass) {
        this(new String[] {}, testClass);
    }

    public JasmineTestInterpreter(String[] libPaths, Class<?> testClass) {
        super(libPaths, testClass);

        try {
            this.runnerFile = FileUtils.extractResourceToTempDirectory(
                    resLoader.getResource("classpath:" + JASMINE_PATH_PREFIX + "jasmine-phantom-runner.js"), 
                    JASMINE_PATH_PREFIX, 
                    true);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Override
    public String getRunnerPath() {
        return runnerFile.getAbsolutePath();
    }

    @Override
    public String getTestHTML(String[] additionalLibraries, String testDataUrl) {
        try {
            InputStream baseHtml = resLoader.getResource(
                    JASMINE_PATH_PREFIX + "jasmine-test-runner.html").getInputStream();
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
    
}
