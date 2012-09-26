package fi.solita.phantomrunner.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.IOUtil;

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
        List<String> result = new ArrayList<>();
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
}
