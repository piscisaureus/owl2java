package de.incunabulum.owl2java.utils;

import java.io.File;
import java.io.IOException;

public class JavaUtils {

	static public String[] java_keywords = {"abstract", "double", "int", "strictfp", "boolean", "else", "interface",
			"super", "break", "extends", "long", "switch", "byte", "final", "native", "synchronized", "case",
			"finally", "new", "this", "catch", "float", "package", "throw", "char", "for", "private", "throws",
			"class", "goto", "protected", "transient", "const", "if", "public", "try", "continue", "implements",
			"return", "void", "default", "import", "short", "volatile", "do", "instanceof", "static", "while"};

	static public final java.util.Set<String> reservedWords = new java.util.HashSet<String>();

	static {
		for (int i = 0; i < java_keywords.length; i++) {
			reservedWords.add(java_keywords[i]);
		}
	}

	public static String toDirectoryFromPackage(String packageName) {
		return packageName.replace(".", "/");
	}

	public static String toDirectoryFromPackage(String packageName, String baseDir) {
		String pkgDir = toDirectoryFromPackage(packageName);
		return baseDir + "/" + pkgDir;
	}

	static public String toValidJavaName(String aName) {
		String retval = aName;

		if (retval != null) {
			retval = retval.replace('-', '_');
			retval = retval.replace('.', '_');
			if (reservedWords.contains(retval)) {
				retval = "_" + retval;
			}
		}
		return retval;

	}

	static public String toValidPackageName(String aName) {
		return JavaUtils.toValidJavaName(aName).toLowerCase();
	}
	
	public static String getCurrentDirectory() {
		String path = "";
		try {
			path = (new File (".")).getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

}
