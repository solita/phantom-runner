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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.IOUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class Strings {

    public static String firstMatch(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        if (!m.find()) {
            throw new IllegalArgumentException("No match found for String: " + str);
        }
        return str.substring(m.start(), m.end());
    }
    
    public static List<String> splitTokens(String str, String regex) {
        return splitTokens(str, regex, 0);
    }
    
    public static List<String> splitTokens(String str, String regex, int flags) {
        Pattern p = Pattern.compile(regex, flags);
        Matcher m = p.matcher(str);
        List<String> result = new ArrayList<String>();
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }
    
    public static String indentLines(String str, int indentAmount) {
        StringBuilder builder = new StringBuilder();
        for (String line : str.split("\n")) {
            for (int i = 0; i < indentAmount; i++) {
                builder.append(' ');
            }
            builder.append(line);
        }
        return builder.toString();
    }

    public static String streamToString(InputStream stream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtil.copy(stream, writer, "UTF-8");
        return writer.toString();
    }
    
    public static List<String> stringList(String...str) {
        return Arrays.asList(str);
    }

    public static List<String> stringList(String str, String... strs) {
        Builder<String> builder = ImmutableList.builder();
        builder.add(str);
        builder.add(strs);
        return builder.build();
    }
}
