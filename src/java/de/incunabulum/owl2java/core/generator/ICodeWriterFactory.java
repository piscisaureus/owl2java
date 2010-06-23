package de.incunabulum.owl2java.core.generator;

import java.io.IOException;
import java.io.Writer;

public interface ICodeWriterFactory {
	/**
	 * Returns a writer that optionally processes data and then writes the result to writer _destination_
	 */
	public abstract Writer getCodeWriter(Writer destination);

	/**
	 * Returns a writer that optionally processes data and then writes the result to file _fileName_
	 */
	public abstract Writer getCodeWriter(String fileName) throws IOException;
}