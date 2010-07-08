package nl.tudelft.tbm.eeni.owl2java.formatter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

/**
 * Stream writer that formats source code
 */
public class CodeFormattingWriter extends StringWriter {
	// The CodeFormatter instance
	CodeFormatter formatter;

	// The writer that we'll write the formatting result to
	Writer destination;

	/**
	 * Creates a new code-formatting wrapper with default formatter settings
	 */
	public CodeFormattingWriter(Writer destination) {
		this(destination, new CodeFormatter());
	}

	/**
	 * Creates a new code-formatting wrapper with specified formatter settings;
	 * if formatterOptions is null, use default formatter settings
	 */
	public CodeFormattingWriter(Writer destination, Properties formatterSettings) {
		this(destination, new CodeFormatter(formatterSettings));
	}

	/**
	 * Creates a new code-formatting wrapper, reusing an existing code formatter
	 * instance
	 */
	public CodeFormattingWriter(Writer destination, CodeFormatter formatter) {
		// Check for null values
		assert destination != null : "destination argument is null";
		assert formatter != null : "formatter argument is null";

		this.destination = destination;
		this.formatter = formatter;
	}

	/**
	 * Formats the contents of the string buffer, then flushes
	 *
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		// Close the string buffer
		super.close();

		// Format the contents of the string buffer
		String formattedCode = formatter.format(getBuffer().toString());

		// Write the formatted code to the destination stream
		destination.write(formattedCode);

		// Close the destination stream
		destination.close();
		this.destination = null;
	}

	/**
	 * Flush is ignored by formattedWriter
	 *
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() {
		// Ignore flush signal
	}
}