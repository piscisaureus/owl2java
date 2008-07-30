package de.incunabulum.owl4java.test;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.incunabulum.owl4java.generator.Generator;

public class Test {
	
	@SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Test.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Date startDate = new Date();

		String uri = "http://owl.incunabulum.de/2007/10/kEquipment.owl";
		//String uri = "http://owl.incunabulum.de/test1.owl";

		// NOTE: reasoning required for properties!
		//OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlModel.read(uri);
		

		Generator gen = new Generator();
		gen.generate(owlModel, "src/test", "model.base.pkg");
		
		Date stopDate = new Date();
		long elapse = stopDate.getTime() - startDate.getTime();
		log.info("Test finished (" + elapse + " ms)");
		

	}

}
