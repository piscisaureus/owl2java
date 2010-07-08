package nl.tudelft.tbm.eeni.demo;

import nl.tudelft.tbm.eeni.owlstructure.processor.FunctionalPropertyInferer;
import nl.tudelft.tbm.eeni.owlstructure.processor.IOntologyProcessor;
import nl.tudelft.tbm.eeni.owlstructure.processor.PropertyDomainInferer;
import nl.tudelft.tbm.eeni.owlstructure.processor.PropertyRangeInferer;
import nl.tudelft.tbm.eeni.owlstructure.processor.PropertyRangeSimplifier;
import nl.tudelft.tbm.eeni.owlstructure.processor.ThingExtender;
import nl.tudelft.tbm.eeni.owlstructure.utils.OntologyUtils;

import com.hp.hpl.jena.ontology.OntModel;

class StructureFinder {

	public static void main(String[] args) {
		try {
			// Load the ontology
			OntModel ontModel = OntologyUtils.loadOntology("file:resources/demo/industries-unstructured.owl");

			// These are the tools that we will use to structure the ontology
			IOntologyProcessor[] structurizers = { new ThingExtender(ThingExtender.Target.TOP_CLASSES), new FunctionalPropertyInferer(), new PropertyDomainInferer(), new PropertyRangeSimplifier(),
				new PropertyRangeInferer() };

			// Run all the processors
			for (IOntologyProcessor preprocessor : structurizers) {
				ontModel = preprocessor.process(ontModel);
			}

			// Now we're going to save the structured ontology
			OntologyUtils.saveOntologyRdf(ontModel, "resources/demo/industries-structured.owl");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}