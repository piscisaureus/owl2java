package de.incunabulum.jakuzi.utils;

public class StringUtils {

	public static String headerStr = "########################################################################";
	public static String preStr = "#### ";
	public static String indent = "  ";

	protected static String indentNextLines(String text, String indent) {
		String origText = new String(text);
		String ret = new String();
		String[] lines = text.split("\\n");
		for (int i = 0; i < lines.length; i++) {
			// last line and no final \n on orig text
			if (i == lines.length -1 && !origText.endsWith("\n"))

				ret += indent + lines[i];
			else
				ret += indent + lines[i] + "\n";
		}
		return ret;
	}

	public static String indentText(String text) {
		text = indentNextLines(text, indent);
		return text;
	}

	public static String indentText(String text, int level) {
		String indent = new String();
		for (int i = 0; i < level; i++) {
			indent += StringUtils.indent;
		}
		text = indentNextLines(text, indent);
		return text;
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
