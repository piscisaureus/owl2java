package de.incunabulum.jakuzi.test;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.incunabulum.jakuzi.generator.Generator;

public class Test {

	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Test.class);
	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Date startDate = new Date();

		Generator gen = new Generator();

		String uri = "http://owl.incunabulum.de/2007/10/kEquipment.owl";
		// gen.generate(uri, "src/test", "model.kequipment");

		uri = "http://owl.incunabulum.de/2008/02/owl4java.owl";
		String file = "file:///D:/workspace/Kns-Owl/2008/02/owl4java.owl";
		//gen.generate(uri, "src/test", "model.owl4java");
			gen.generate(uri, file, "src/test", "model.owl4java");

		// report
		String report = gen.getReport();
		//log.error(report);

		report = gen.getStatistics();
		log.error(report);

		// for (int i = 0; i < 5 ; i++) {
		// gen.generate(owlModel, "src/test", "model.base.pkg");
		// }

		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");

		
	}
	


}
