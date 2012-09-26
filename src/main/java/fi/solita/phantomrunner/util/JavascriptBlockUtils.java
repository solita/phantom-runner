package fi.solita.phantomrunner.util;

import java.util.ArrayList;
import java.util.List;

public class JavascriptBlockUtils {

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
