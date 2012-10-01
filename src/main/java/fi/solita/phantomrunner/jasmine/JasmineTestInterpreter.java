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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.HeadTag;
import org.htmlparser.tags.Html;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
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
    
    private final Map<String, String> libFilePathCache = new HashMap<>();
    private final Map<String, String> libFileContentCache = new HashMap<>();
    
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
            
            NodeList html = new Parser(new Lexer(new Page(baseHtml, "UTF-8"))).parse(null);
            NodeList head = findHeadContent(html);
            
            // Interpreter libraries usually come from inside a jar-package and thus they cannot be 
            // referenced via file system URI. Because of this we'll inject the scripts themselves 
            // directly to the HTML.
            for (String lib : getLibPaths()) {
                injectScript(head, lib);
            }

            for (String lib : additionalLibraries) {
                appendScript(head, lib);
            }

            appendScript(head, testDataUrl);

            return html.toHtml();
        } catch (IOException | ParserException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private NodeList findHeadContent(NodeList html) {
        return findChildren(findChildren(html, Html.class), HeadTag.class);
    }

    private NodeList findChildren(NodeList nodes, Class<? extends Tag> tagClass) {
        SimpleNodeIterator iter = nodes.elements();
        while (iter.hasMoreNodes()) {
            Node n = iter.nextNode();
            if (n.getClass().equals(tagClass)) {
                return n.getChildren();
            }
        }
        throw new IllegalArgumentException(String.format("No tag %s found", tagClass));
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

    private void appendScript(NodeList head, String libPath) throws IOException {
        // really, how bad a HTML parsing library can be? Unfortunately JSOUP is even worse when it comes
        // to dynamically adding JavaScript content due to it enforces stripping end of line characters away
        ScriptTag script = new ScriptTag();
        script.setAttribute("type", "text/javascript");
        
        String path = libFilePathCache.get(libPath);
        if (path == null) {
            path = resLoader.getResource(libPath).getFile().getAbsolutePath();
            libFilePathCache.put(libPath, path);
        }
        script.setAttribute("src", path);
        head.add(script);
        // really? ONLY way to close the script tag without xml ending?
        head.add(new TextNode("</SCRIPT>\n"));
    }

    private void injectScript(NodeList head, String libPath) throws IOException {
        ScriptTag script = new ScriptTag();
        script.setAttribute("type", "text/javascript");
        
        String content = libFileContentCache.get(libPath);
        if (content == null) {
            content = Strings.streamToString(resLoader.getResource(libPath).getInputStream());
            libFileContentCache.put(libPath, content);
        }
        script.setScriptCode(content);
        head.add(script);
        head.add(new TextNode("</SCRIPT>\n"));
    }
    
}
