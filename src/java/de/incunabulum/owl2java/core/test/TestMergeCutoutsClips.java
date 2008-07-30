package de.incunabulum.owl2java.core.test;

import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import db4otest.cutoutsclips.owl2java.MergeCode;

public class TestMergeCutoutsClips extends TestCase{

	public static void testMergeCutoutClips() {
		String uri = "http://owl.incunabulum.de/2008-Base/cutoutsClips.owl";
		OntModel owlModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		OntDocumentManager owlDocMgr = owlModel.getDocumentManager();
		owlDocMgr.setProcessImports(true);
		owlModel.read(uri);

		MergeCode mc = new MergeCode();
		mc.run(owlModel, "src/testOut/cutoutclips.db4o");
	}
	
	

}
