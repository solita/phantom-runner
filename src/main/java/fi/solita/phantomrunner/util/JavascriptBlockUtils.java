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
package fi.solita.phantomrunner.util;

import java.util.ArrayList;
import java.util.List;

public class JavascriptBlockUtils {

    /**
     * <p>Simple utility method for parsing a file for specific code blocks. It searches the file for keyword
     * and then finds the ending brace for that specific code block. This is repeated for all found such
     * blocks and the result is returned as a List.</p>
     * 
     * <p>Note, this is a poor man's version of a real proper AST. It has several problems and should be used
     * with caution.</p> 
     */
    public static List<String> findBlocks(String data, String keyword) {
        List<String> elements = new ArrayList<>();
        
        for (int keywordIndex = data.indexOf(keyword); keywordIndex != -1; keywordIndex = data.indexOf(keyword, keywordIndex+1)) {
            int blockEndIndex = data.indexOf(");", findClosingBrace(findOpeningBrace(keywordIndex, data), data));
            elements.add(data.substring(keywordIndex, blockEndIndex + ");".length()));
        }
        
        return elements;
    }
    
    private static int findOpeningBrace(int describeIndex, String data) {
        for (int i = describeIndex; i < data.length(); i++) {
            if (data.charAt(i) == '{') {
                return i;
            }
        }
        throw new IllegalArgumentException("No opening brace found");
    }

    private static int findClosingBrace(int openingBraceIndex, String data) {
        int bracesOpen = 0;
        for (int i = openingBraceIndex; i < data.length(); i++) {
            if (data.charAt(i) == '{') {
                bracesOpen++;
            } else if (data.charAt(i) == '}' && bracesOpen > 1) {
                bracesOpen--;
            } else if (data.charAt(i) == '}' && bracesOpen == 1) {
                return i;
            }
        }
        throw new IllegalArgumentException("No closing brace found");
    }
}
