package nl.tudelft.tbm.eeni.owlstructure.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import nl.tudelft.tbm.eeni.owlstructure.utils.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Simplify complex intersectionOf / unionOf property ranges (as created by a.o. Protege)
 * to a simple flat list of named classes, and deletes the anonymous classes from the ontology.
 *
 * This is needed because Owl2Java chokes on complex property ranges;
 * therefore maybe this should be integrated in Owl2Java?
 */
public class PropertyRangeSimplifier implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	/**
	 * Creates a new property range simplifier
	 * @see PropertyRangeSimplifier
	 */
	public PropertyRangeSimplifier() {
	}

	/**
	 * Run the propery range inferer on all classes in the given ontology
	 *
	 * @param ontModel	The ontology model to work on
	 */
	@Override
	public OntModel process(OntModel ontModel) {
		// Loop over all properties
		Collection<OntProperty> properties = ontModel.listAllOntProperties().toList();
		for(OntProperty property: properties) {
			// Find existing ranges
			Collection<Resource> oldRanges = new HashSet<Resource>(property.listRange().toList());

			// Simplify the range list to a flat list of named classes
			Collection<Resource> newRanges = simplifyRange(ontModel, oldRanges);

			// Remove existing ranges from property (retained ranges will be included in newRanges and thus re-added)
			for (Resource range: oldRanges) {
				property.removeRange(range);
			}
			// Add new ranges to property
			for (Resource range: newRanges) {
				property.addRange(range);
			}

			// Debug output
			log.info("Property range simplification for property: " + property.getLocalName() + "\n"
					+ getLogMessage("retaining range(s)", CollectionUtils.intersectCollections(oldRanges, newRanges)) + "\n"
					+ getLogMessage("adding range(s)", CollectionUtils.subtractCollections(newRanges, oldRanges)) + "\n"
					+ getLogMessage("removing range(s)", CollectionUtils.subtractCollections(oldRanges, newRanges)));

		}

		return ontModel;
	}

	/**
	 * Given a certain property, list what datatypes (boolean, double, string, etc)
	 * or classes are used for the values that instances refer to using this property.
	 */
	private Collection<Resource> simplifyRange(OntModel ontModel, Collection<Resource> oldRanges) {
		HashSet<Resource> newRanges = new HashSet<Resource>();

		/*
		 * If keepExistingRanges is enabled, start with current ranges already assigned in the property range
		 */
		// Loop over all existing ranges
		Iterator<Resource> rangeIterator = oldRanges.iterator();
		while (rangeIterator.hasNext()) {
			Resource range = rangeIterator.next();
			// See whether it is a class or not
			if (range.canAs(OntClass.class)) {
				// It is a class, cast it
				OntClass rangeClass = range.as(OntClass.class);

				// Check whether it is an anonymous class
				if (rangeClass.isAnon()) {
					// Handle intersection class
					if (rangeClass.isIntersectionClass()) {
						// Cast to IntersectionClass
						IntersectionClass intersectionClass = rangeClass.asIntersectionClass();
						// Add operands to range
						newRanges.addAll(simplifyRange(ontModel, new HashSet<Resource>(intersectionClass.listOperands().toSet())));
						// Delete anonymous IntersectionClass from the ontology
						intersectionClass.remove();
						continue;
					}
					// Handle union class
					if (rangeClass.isUnionClass()) {
						// Cast to UnionClass
						UnionClass unionClass = rangeClass.asUnionClass();
						// Add operands to range
						newRanges.addAll(simplifyRange(ontModel, new HashSet<Resource>(unionClass.listOperands().toSet())));
						// Delete anonymous UnionClass from the ontology
						unionClass.remove();
						continue;
					}
				}
			}

			// It wasn't an anonymous boolean class, so just keep it
			newRanges.add(range);
		}

		return newRanges;
	}

	/**
	 * Format a debug message containing a message and a list of resource localNames
	 */
	private String getLogMessage(String message, Collection<? extends Resource> resources) {
		String result = "  - " + message + ": ";
		if (resources.size() > 0) {
			int counter = 0;
			for (Resource resource: resources) {
				result += (counter++ > 0 ? ", " : "") + resource.getLocalName();
			}
		} else {
			result += "«none»";
		}
		return result;
	}

}