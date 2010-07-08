package nl.tudelft.tbm.eeni.owl2java.test;

import java.util.Date;

import junit.framework.TestCase;

import nl.tudelft.tbm.eeni.owl2java.JenaGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TestGenerators extends TestCase{

	private static Log log = LogFactory.getLog(TestGenerators.class);

	void main() {
		testJenaGeneratorFull();
		testJenaGeneratorSimple();
	}
	
	public static void testJenaGeneratorFull() {
		Date startDate = new Date();

		JenaGenerator gen = new JenaGenerator();

		String uri = "file:resources/test/owl4java.owl";
		gen.generate(uri, "testOut", "jenatestFull");

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

		String uri = "file:resources/test/owl4java-simple.owl";
		gen.generate(uri, "testOut", "jenatestSimple");

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
