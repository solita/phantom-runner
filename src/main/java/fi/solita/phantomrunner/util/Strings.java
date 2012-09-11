package fi.solita.phantomrunner.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

	public static String firstMatch(String str, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(str);
		if (!m.find()) {
			throw new IllegalArgumentException("No match found for String: " + str);
		}
		return str.substring(m.start(), m.end());
	}
}
