package nl.tudelft.tbm.eeni.owlstructure.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import nl.tudelft.tbm.eeni.owlstructure.utils.CollectionUtils;
import nl.tudelft.tbm.eeni.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

/**
 * Set or extend property ranges by looking at instances in a given ontology
 */
public class PropertyRangeInferer implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	private boolean keepExistingRanges;
	private boolean allowThingRange;

	/**
	 * Creates a new property range inferer, that infers property ranges by
	 * looking at instances in the ontology.
	 *
	 * @param keepExistingRanges
	 *            Whether to keep ranges already defined in the ontology.
	 *            An existing owl:Thing range will still be removed when
	 *            keepExistingRanges is set to false. Defaults to true.
	 * @param allowThingRange
	 *            Whether the range may contain the class owl:Thing. Defaults to false.
	 */
	public PropertyRangeInferer(boolean keepExistingRanges, boolean allowThingRange) {
		this.keepExistingRanges = keepExistingRanges;
		this.allowThingRange = allowThingRange;
	}

	/**
	 * Creates a new property range inferer, that infers property ranges by
	 * looking at instances in the ontology.
	 *
	 * @param keepExistingRanges
	 *            Whether to keep ranges already defined in the ontology.
	 *            An existing owl:Thing range will still be removed when
	 *            keepExistingRanges is set to false. Defaults to true.
	 */
	public PropertyRangeInferer(boolean keepExistingRanges) {
		this(keepExistingRanges, false);
	}

	/**
	 * Creates a new property range inferer, that infers property ranges by
	 * looking at instances in the ontology.
	 */
	public PropertyRangeInferer() {
		this(true, false);
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
			// Find what classes/datatypes this property is used to refer to
			Collection<Resource> newRanges = findPropertyRanges(ontModel, property);

			// Remove existing ranges from property (retained ranges will be included in newRanges and thus re-added)
			for (Resource range: oldRanges) {
				property.removeRange(range);
			}
			// Add new ranges to property
			for (Resource range: newRanges) {
				property.addRange(range);
			}

			// Debug output
			log.info("Property range inference for property: " + property.getLocalName() + "\n"
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
	private Collection<Resource> findPropertyRanges(OntModel ontModel, OntProperty property) {
		// Use separate lists to store datatypes and classes in the property range
		HashSet<Resource> rangeDatatypes = new HashSet<Resource>();
		HashSet<OntClass> rangeClasses = new HashSet<OntClass>();

		/*
		 * If keepExistingRanges is enabled, start with current ranges already assigned in the property range
		 */
		if (this.keepExistingRanges) {
			// Loop over all existing ranges
			Iterator<? extends Resource> rangeIterator = property.listRange();
			while (rangeIterator.hasNext()) {
				Resource range = rangeIterator.next();
				// See whether it is a class or not
				if (range.canAs(OntClass.class)) {
					// It is a class
					rangeClasses.add(range.as(OntClass.class));
				} else {
					// It is a datatype (or maybe something else?)
					rangeDatatypes.add(range);
				}
			}
		}


		/*
		 * Search ontology instances to find out what this property is used for in practice, and then
		 * - add all found literal types to rangeDatatypes;
		 * - add those classes found to rangeClasses that are the at least once the
		 *   most specific class of an instance that this property refers to;
		 */
		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select distinct ?class (datatype(?o) as ?datatype) "
				+ "where { "
				+ "  ?s <" + property.getURI() + "> ?o . "
				+ "  optional { ?o rdf:type ?class } . "
				+ "} ";
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
			QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

			ResultSet results = qexec.execSelect();
				while(results.hasNext()) {
					QuerySolution result = results.nextSolution();

					// See what type the property refers to
					if (result.contains("datatype")) {
						// Found this property with a a typed literal value
						rangeDatatypes.add(result.get("datatype").as(Resource.class));
					} else if (result.contains("class")) {
						/*
						 *  Found this property referring to another instance of a certain class
						 *  We want to make sure that the property refers to a direct instance of the found class,
						 *  and not an instance of a *subclass* of the found class
						 */
						OntClass ontClass = result.get("class").as(OntClass.class);
						if (anyInstanceRefersToDirectClassInstance(ontModel, property, ontClass)) {
							// It does refer to a direct instance of this class, so add it
							rangeClasses.add(ontClass);
						}
					} else {
						// It refers to something else, we can't handle this right now
						// @TODO how to deal with unknown-class URIs / plain literals / blank nodes?
					}
				}
				qexec.close();
		} finally {
			ontModel.leaveCriticalSection();
		}

		/*
		 * If owl:Thing is not allowed to be used as range, remove it from the range classes list
		 */
		if (!allowThingRange) {
			rangeClasses.remove(OntologyUtils.getOwlThing(ontModel));
		}

		/*
		 * For each class found to be in range of this property, filter out all subclasses
		 * that have this class in its inheritance chain.
		 * Subclasses that have multiple (indirect) superclasses are retained,
		 * unless *all* of its ancestry branches are found within the property range.
		 *
		 */
		for (OntClass ontClass: rangeClasses.toArray(new OntClass[]{})) {
			if (OntologyUtils.containsCompleteClassSuperset(ontClass, rangeClasses)) {
				rangeClasses.remove(ontClass);
			}
		}

		// Combine rangeClasses and rangeDatatypes into the final range list
		HashSet<Resource> ranges = new HashSet<Resource>();
		ranges.addAll(rangeDatatypes);
		ranges.addAll(rangeClasses);

		// Return the final range list
		return ranges;
	}

	/**
	 * Find out whether there is instance that uses this property to refer to a *direct* instance of this class.
	 * A direct instance is an instance that has a certain class *but not* any of its subclasses.
	 */
	private boolean anyInstanceRefersToDirectClassInstance(OntModel ontModel, OntProperty property, OntClass ontClass) {
		// Find all descendants of this class that we want to exclude
		Collection<OntClass> descendants = OntologyUtils.listClassDescendants(ontClass);

		if (descendants.isEmpty()) {
			// If the class has no descendants at all, all instances must be direct instances
			return true;

		} else {
			/*
			 *  If the class does have descendants, find instances referred to by the property that
			 *  are an instance of the given class but not an instance of any of its descendants.
			 */
			String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
					+ "select (count(?s) as ?count) "
					+ "where { "
					+ "  ?s <" + property.getURI() + "> ?o . "
					+ "  ?o rdf:type <" + ontClass.getURI() + "> . ";
			for (OntClass descendant : descendants) {
				queryString += "  unsaid { ?o rdf:type <" + descendant.getURI() + "> } . ";
			}
			queryString += "} ";

			ontModel.enterCriticalSection(Lock.READ);
			int instanceCount;
			try {
				Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
				QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

				ResultSet results = qexec.execSelect();
				if (results.hasNext()) {
					instanceCount = results.nextSolution().getLiteral("count").getInt();
				} else {
					instanceCount = 0;
				}
				OntologyUtils.closeIterator(results);
				qexec.close();
			} finally {
				ontModel.leaveCriticalSection();
			}

			if (instanceCount > 0) {
				// We did find any direct class instances referred to using property
				return true;
			} else {
				// We didn't find any of those instances
				return false;
			}
		}
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