package nl.tudelft.tbm.eeni.owl2java.formatter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/**
 * Wrapper class for Eclipse JDT code formatter
 */
public class CodeFormatter {
	// Location of default settings file, relative to this class
	private final static String DEFAULT_SETTINGS_FILE = "formatter-defaults.properties";

	// Log message emitter
	private static Log log = LogFactory.getLog(CodeFormattingWriter.class);

	// Settings map for JDT code formatter
	private Properties formatterSettings;


	/**
	 * Constructor for CodeFormatter with default options
	 */
	public CodeFormatter() {
		this(null);
	}

	/**
	 * Constructor for CodeFormatter with given settings;
	 * if given null, loads default formatter settings
	 */
	public CodeFormatter(Properties formatterSettings) {
		setFormatterSettings(formatterSettings);
	}

	/**
	 * Formats source code using current formatter settings
	 */
	public String format(String code) {

		/*
		 * The settings map must at least contain these three options, or the JDT code formatter will not work.
		 * Check if these settings exist, otherwise emit a warning and return the unformatted source code.
		 */
		if (!formatterSettings.containsKey(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM)) {
			log.warn("Code formatter settings must define " + JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM);
			return code;
		}
		if (!formatterSettings.containsKey(JavaCore.COMPILER_COMPLIANCE)) {
			log.warn("Code formatter settings must define " + JavaCore.COMPILER_COMPLIANCE);
			return code;
		}
		if (!formatterSettings.containsKey(JavaCore.COMPILER_SOURCE)) {
			log.warn("Code formatter settings must define " + JavaCore.COMPILER_SOURCE);
			return code;
		}

		// Load the code into an eclipse document
		IDocument document = new Document();
		document.set(code);

		// Create the code formatter
		org.eclipse.jdt.core.formatter.CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(this.formatterSettings);

		/*
		 *  Run the formatter and apply the modifications to the document.
		 *  If it fails (probably due to a syntax error), just return the unformatted source code.
		 */
		TextEdit modifications = codeFormatter.format(org.eclipse.jdt.core.formatter.CodeFormatter.K_COMPILATION_UNIT,
				code, 0, code.length(), 0, null);
		if (modifications != null) {
			try {
				modifications.apply(document);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// Return the document contents as string
		return document.get();
	}

	/**
	 * Returns a Properties object representing containing all the code formatter options
	 */
	public Properties getFormatterSettings() {
		return formatterSettings;
	}

	/**
	 * Sets the formatter options;
	 * if given null, reverts to default formatter options
	 */
	public void setFormatterSettings(Properties formatterSettings) {
		if (formatterSettings != null) {
			this.formatterSettings = formatterSettings;
		} else {
			this.formatterSettings = getDefaultSettings();
		}
	}

	/**
	 * Loads default options from DEFAULT_OPTIONS_FILE
	 */
	private static Properties getDefaultSettings() {
		try {
			InputStream inputStream = CodeFormattingWriter.class.getResourceAsStream(DEFAULT_SETTINGS_FILE);
			if (inputStream != null) {
				Properties options = new Properties();
				options.load(inputStream);
				return options;
			} else {
				throw new FileNotFoundException(DEFAULT_SETTINGS_FILE);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}