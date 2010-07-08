package nl.tudelft.tbm.eeni.owlstructure.processor;

import java.util.Collection;

import nl.tudelft.tbm.eeni.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

public class ThingExtender implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	public static enum Target {
		/* let all classes without superclasses extend owl:Thing */
		TOP_CLASSES,
		/* let all classes extend owl:Thing */
		ALL_CLASSES
	}

	private Target target;

	public ThingExtender(Target topClasses) {
		this.target = topClasses;
	}

	public ThingExtender() {
		this(Target.TOP_CLASSES);
	}

	@Override
	public OntModel process(OntModel ontModel) {
		// Fetch or create the owl:Thing class
		OntClass Thing = OntologyUtils.getOwlThing(ontModel);

		// Copy all classes to a list first to avoid ConcurrentModification exceptions
		Collection<OntClass> ontClasses = ontModel.listClasses().toList();

		for (OntClass ontClass : ontClasses) {
			// We shouldn't extend Thing into Thing
			if (!ontClass.equals(Thing)) {
				// Make ontClass extend Thing if...
				// (1) the target is ALL_CLASSES and ontClass does not already have superclass Thing
				// (2) the target is TOP_CLASSES and ontClass has no supers
				switch (target) {
					case ALL_CLASSES:
						if (!ontClass.hasSuperClass(Thing)) {
							log.info("Adding owl:Thing superclass to " + ontClass.getLocalName());
							ontClass.addSuperClass(Thing);
						}
						break;

					case TOP_CLASSES:
						if (ontClass.listSuperClasses().toList().isEmpty()) {
							log.info("Adding owl:Thing superclass to " + ontClass.getLocalName());
							ontClass.addSuperClass(Thing);
						}
						break;
				}
			}
		}

		// Return the processed ontology
		return ontModel;
	}
}
