package de.incunabulum.owl4java.test;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;

import de.incunabulum.owl4java.generator.Generator;

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
		String uri = "http://owl.incunabulum.de/test1.owl";
		//gen.generate(uri, "src/test", "model.test1");

		uri = "http://owl.incunabulum.de/2007/10/kEquipment.owl";
		gen.generate(uri, "src/test", "model.kequipment");

		uri = "http://owl.incunabulum.de/2008/02/owl4java.owl";
		gen.generate(uri, "src/test", "model.owl4java");

		// report
		String report = gen.getReport();
		log.error(report);

		report = gen.getStatistics();
		log.error(report);

		// for (int i = 0; i < 5 ; i++) {
		// gen.generate(owlModel, "src/test", "model.base.pkg");
		// }

		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");
	
		// type mapper test
		TypeMapper tm = TypeMapper.getInstance();
		Iterator it = tm.listTypes();
		while (it.hasNext()) {
			RDFDatatype dt = (RDFDatatype) it.next();
			log.info(dt);
			
		}
		
	}
	


}
