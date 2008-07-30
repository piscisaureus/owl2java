package de.incunabulum.owl2java.test;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.JenaGenerator;

public class GenerateJena {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(GenerateJena.class);
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Date startDate = new Date();

		JenaGenerator gen = new JenaGenerator();

		String uri = "http://owl.incunabulum.de/2008-Jakuzi/owl4java.owl";
		gen.generate(uri, "src/testOut", "jenatest");

		// report
		String report = gen.getJModelReport();
		//log.error(report);

		report = gen.getStatistics();
		log.info(report);

		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");

		
	}
	


}
