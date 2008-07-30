package de.incunabulum.owl2java.test;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.Db4oGenerator;

public class GenerateDb4o {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(GenerateDb4o.class);

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Date startDate = new Date();

		Db4oGenerator gen = new Db4oGenerator();
		// gen.setGenerateMergeCode(true);
		// gen.setGenerationType(Db4oGenerator.ClassBasedGeneration);
		// gen.setInstanceClassName("MergeCode");

		String uri = "";

		// simple code, not using multiple inheritance
		uri = "http://owl.incunabulum.de/2008-Jakuzi/owl4java-simple.owl";
		gen.generate(uri, "src/testOut", "db4otest.classbased");

		uri = "http://owl.incunabulum.de/2008-Base/cutoutsClips.owl";
		gen.generate(uri, "src/testOut", "db4otest.cutoutsclips");

		// report
		String report = gen.getJModelReport();
		// log.error(report);

		report = gen.getStatistics();
		log.error(report);
		log.error(gen.getJModelReport());

		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");

	}

}
