package de.incunabulum.owl2java.core.generator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public abstract class AbstractCodeWriterFactory implements ICodeWriterFactory {
	/**
	 * @see de.incunabulum.owl2java.core.generator.ICodeWriterFactory#getCodeWriter(java.io.Writer)
	 */
	public abstract Writer getCodeWriter(Writer destination);

	/**
	 * @see de.incunabulum.owl2java.core.generator.ICodeWriterFactory#getCodeWriter(java.lang.String)
	 */
	public final Writer getCodeWriter(String fileName) throws IOException {
		// Create a file writer for the given filename
		Writer writer = new FileWriter(fileName);
		// Hand the file writer over to the stream writer wrapper
		writer = getCodeWriter(writer);
		// Return the resulting writer
		return writer;
	}
}
