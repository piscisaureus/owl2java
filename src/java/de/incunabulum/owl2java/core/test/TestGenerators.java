package de.incunabulum.owl2java.core.test;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.owl2java.core.Db4oGenerator;
import de.incunabulum.owl2java.core.JenaGenerator;

public class TestGenerators extends TestCase{

	private static Log log = LogFactory.getLog(TestGenerators.class);

	public static void testJenaGeneratorFull() {
		Date startDate = new Date();

		JenaGenerator gen = new JenaGenerator();

		String uri = "http://owl.incunabulum.de/2008-Jakuzi/owl4java.owl";
		gen.generate(uri, "src/testOut", "jenatestFull");

		// report
		String report = gen.getJModelReport();
		//log.error(report);

		report = gen.getStatistics();
		log.info(report);

		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");
	}
	
	public static void testJenaGeneratorSimple() {
		Date startDate = new Date();

		JenaGenerator gen = new JenaGenerator();

		String uri = "http://owl.incunabulum.de/2008-Jakuzi/owl4java-simple.owl";
		gen.generate(uri, "src/testOut", "jenatestSimple");

		// report
		String report = gen.getJModelReport();
		//log.error(report);

		report = gen.getStatistics();
		log.info(report);

		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");
	}

	public static void testDb4oGenerator() {
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
