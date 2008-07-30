package de.incunabulum.owl4java.utils;

public class StringUtils {

	public static String headerStr = "########################################################################";
	public static String preStr = "#### ";
	public static String indent = "  ";

	public static String indentText(String text) {
		return StringUtils.indent + text;
	}

	public static String indentText(String text, int level) {
		String out = new String();
		for (int i = 0; i < level; i++) {
			out += indentText(new String());
		}
		return out + text;
	}

	public static String toFirstLowerCase(String string) {
		return string.substring(0, 1).toLowerCase() + string.substring(1);
	}

	public static String toFirstUpperCase(String string) {
		if (string != "")
			return string.substring(0, 1).toUpperCase() + string.substring(1);
		return string;
	}
	public static String toHeader(String text) {
		String str = "\n" + StringUtils.headerStr + "\n" + StringUtils.preStr + text + "\n" + StringUtils.headerStr
				+ "\n";
		return str;
	}
	public static String toSubHeader(String text) {
		String str = "\n" + StringUtils.preStr + text + "\n";
		return str;
	}

}
