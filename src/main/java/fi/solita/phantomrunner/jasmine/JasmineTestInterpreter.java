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
        this(new String[] {"classpath:" + JASMINE_PATH_PREFIX + "jasmine.js"}, testClass);
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
