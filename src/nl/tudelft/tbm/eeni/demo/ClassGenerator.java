package nl.tudelft.tbm.eeni.demo;

import nl.tudelft.tbm.eeni.owl2java.JenaGenerator;
import nl.tudelft.tbm.eeni.owlstructure.processor.PropertyRangeSimplifier;
import nl.tudelft.tbm.eeni.owlstructure.utils.OntologyUtils;

import com.hp.hpl.jena.ontology.OntModel;

class ClassGenerator {

	public static void main(String[] args) {
		try {
			// Load example ontology
			OntModel ontModel = OntologyUtils.loadOntology("file:resources/demo/industries-application.owl");

			// Simplify the definition of property ranges
			// This is necessary because owl2java chokes on complex range
			// definitions (i.e. those containing anonymous classes)
			(new PropertyRangeSimplifier()).process(ontModel);

			// Generate classes that provide access to ontology instances
			JenaGenerator generator = new JenaGenerator();
			generator.generate(ontModel, "src", "nl.tudelft.tbm.eeni.demo.ont");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
