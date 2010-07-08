package nl.tudelft.tbm.eeni.owlstructure.processor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nl.tudelft.tbm.eeni.owlstructure.utils.CollectionUtils;
import nl.tudelft.tbm.eeni.owlstructure.utils.OntologyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;

/**
 * Set or extend property domains by looking at instances in a given ontology
 */
public class PropertyDomainInferer implements IOntologyProcessor {

	static Log log = LogFactory.getLog(FunctionalPropertyInferer.class);

	private boolean keepExistingDomains;
	private boolean enableGeneralizion;
	private boolean allowThingDomain;

	/**
	 * Creates a new property domain inferer, that infers property domains by
	 * looking at instances in the ontology.
	 *
	 * @param keepExistingDomains
	 *            Whether to keep domains already defined in the ontology. An
	 *            existing domain may still be removed when the generalizer
	 *            decides to add a superclass of the existing domain class to
	 *            the domain. An existing owl:Thing domain will be removed when
	 *            allowThingDomain is set to false. Defaults to true.
	 * @param enableGeneralization
	 *            Whether to use the generalization engine is used to find
	 *            abstract superclasses of domain classes. Defaults to true.
	 * @param allowThingDomain
	 *            Whether the domain may contain owl:Thing. Defaults to false.
	 */
	public PropertyDomainInferer(boolean keepExistingDomains, boolean enableGeneralization, boolean allowThingDomain) {
		this.keepExistingDomains = keepExistingDomains;
		this.enableGeneralizion = enableGeneralization;
		this.allowThingDomain = allowThingDomain;
	}

	/**
	 * Creates a new property domain inferer, that infers property domains by
	 * looking at instances in the ontology.
	 *
	 * @param keepExistingDomains
	 *            Whether to keep domains already defined in the ontology. An
	 *            existing domain may still be removed when the generalizer
	 *            decides to add a superclass of the existing domain class to
	 *            the domain. An existing owl:Thing domain will be removed when
	 *            allowThingDomain is set to false. Defaults to true.
	 * @param enableGeneralization
	 *            Whether to use the generalization engine is used to find
	 *            abstract superclasses of domain classes. Defaults to true.
	 */
	public PropertyDomainInferer(boolean keepExistingDomains, boolean enableGeneralization) {
		this(keepExistingDomains, enableGeneralization, false);
	}

	/**
	 * Creates a new property domain inferer, that infers property domains by
	 * looking at instances in the ontology.
	 *
	 * @param keepExistingDomains
	 *            Whether to keep domains already defined in the ontology. An
	 *            existing domain may still be removed when the generalizer
	 *            decides to add a superclass of the existing domain class to
	 *            the domain. An existing owl:Thing domain will be removed when
	 *            allowThingDomain is set to false. Defaults to true.
	 */
	public PropertyDomainInferer(boolean keepExistingDomains) {
		this(keepExistingDomains, true);
	}

	/**
	 * Creates a new property domain inferer, that infers property domains by
	 * looking at instances in the ontology.
	 */
	public PropertyDomainInferer() {
		this(true);
	}

	/**
	 * Run the property domain inferer on all properties in the given ontology.
	 *
	 * @param ontModel	The ontModel instance to work on.
	 */
	@Override
	public OntModel process(OntModel ontModel) {
		// Loop over all properties
		Collection<OntProperty> properties = ontModel.listAllOntProperties().toList();
		for(OntProperty property: properties) {
			// Find existing domains
			Collection<? extends OntResource> oldDomains = property.listDomain().toList();
			// Find classes that use this property
			Collection<OntClass> newDomains = findPropertyDomains(ontModel, property);

			// Remove existing domains from property (retained domains will be included in newDomains and thus re-added)
			for (OntResource domainClass: oldDomains) {
				property.removeDomain(domainClass);
			}
			// Add new domains to property
			for (OntResource domainClass: newDomains) {
				property.addDomain(domainClass);
			}

			// Debug output
			log.info("Property domain inference for property: " + property.getLocalName() + " \n" 
					+ getLogMessage("retaining domain(s)", CollectionUtils.intersectCollections(oldDomains, newDomains)) + "\n"
					+ getLogMessage("adding domain(s)", CollectionUtils.subtractCollections(newDomains, oldDomains)) + "\n"
					+ getLogMessage("removing domain(s)", CollectionUtils.subtractCollections(oldDomains, newDomains)));
		}

		return ontModel;
	}

	/**
	 * Find the classes of things that use this property
	 */
	private Collection<OntClass> findPropertyDomains(OntModel ontModel, OntProperty property) {
		HashSet<OntClass> domainClasses = new HashSet<OntClass>();

		/*
		 * If keepExistingDomains is enabled, start with classes that are already assigned to this class's domain
		 */
		if (keepExistingDomains) {
			Iterator<? extends OntResource> domainIterator = property.listDomain();
			while (domainIterator.hasNext()) {
				domainClasses.add(domainIterator.next().as(OntClass.class));
			}
		}

		/*
		 * Look for classes that use the given property.
		 * For all classes found, check whether this class uses the property directly;
		 * e.g. there are instances of this class and not of any subclass of it that use this property.
		 */
		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select distinct ?domain "
				+ "where { "
				+ "  ?s <" + property.getURI() + "> ?o . "
				+ "  ?s rdf:type ?domain . "
				+ "} ";
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);

			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				OntClass domainClass = results.nextSolution().get("domain").as(OntClass.class);

				// See whether there are instances of ontClass that are not an instance of a subclass that use this property
				if (someDirectInstancesHaveProperty(ontModel, property, domainClass)) {
					domainClasses.add(domainClass.as(OntClass.class));
				}
			}
			qexec.close();
		} finally {
			ontModel.leaveCriticalSection();
		}

		/*
		 * Look for abstract superclasses that should be in the domain of this property.
		 * These are classes that satisfy these requirements:
		 *   (a) it must have multiple disjoint subclasses,
		 *       e.g. subclasses that are not an indirect subclass of one another;
		 *   (b) all of its (indirect) instances either
		 *       (1) have this property, or
		 *       (2) have a superclass that already has the property in its domain
		 */
		if (this.enableGeneralizion) {
			Set<OntClass> allClasses = ontModel.listClasses().toSet();
			for (OntClass ontClass: allClasses) {
				/*
				 * Look if this class has at least two *disjoint* indirect subclasses that use this property.
				 * Multiple classes that are in the same inheritance branch (i.e. one class is (indirect) parent to the other) count as one.
				 */
				Set<OntClass> branches = new HashSet<OntClass>(OntologyUtils.listClassDescendants(ontClass));
				branches.retainAll(domainClasses);
				Iterator<OntClass> branchIterator = domainClasses.iterator();
				while (branchIterator.hasNext()) {
					OntClass branch = branchIterator.next();
					if (OntologyUtils.containsClassSuperset(branch, branches)) {
						branchIterator.remove();
					}
				}
				if (branches.size() > 1) {
					/*
					 *  Check if *all* indirect instances of this class have the property,
					 *  but ignore subclasses that already use this property for sure because
					 *  at least some *direct* instances of that subclass are in the property's domain.
					 */
					if (allIndirectInstancesHaveProperty(ontModel, property, ontClass, domainClasses)) {
						// All conditions are satisfied.
						domainClasses.add(ontClass);
					}
				}

			}
		}

		/*
		 * If owl:Thing is not allowed to be in the domain, remove it
		 */
		if (!this.allowThingDomain) {
			domainClasses.remove(OntologyUtils.getOwlThing(ontModel));
		}

		/*
		 * Remove classes that have at least one superclass that is also in the domain of this property,
		 * because then the inclusion of the subclass is implied by the inclusion of the superclass.
		 */
		Iterator<OntClass> classIterator = domainClasses.iterator();
		while (classIterator.hasNext()) {
			OntClass domainClass = classIterator.next();
			if (OntologyUtils.containsClassSuperset(domainClass, domainClasses)) {
				classIterator.remove();
			}
		}

		return domainClasses;
	}

	private boolean someDirectInstancesHaveProperty(OntModel ontModel, OntProperty property, OntClass ontClass) {
		// Find all descendants of this class
		Collection<OntClass> descendants = OntologyUtils.listClassDescendants(ontClass);

		// Look for instances of this class that have this property, but that are not also of a type that is a subclass of this class.
		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select (count(?o) as ?count) "
				+ "where { "
				+ "  ?s <" + property.getURI() + "> ?o . "
				+ "  ?s rdf:type <" + ontClass.getURI() + "> . ";
		for (OntClass descendant : descendants) {
			queryString += "  unsaid { ?s rdf:type <" + descendant.getURI() + "> } . ";
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

		// If any instances, then there must be at least one direct instance that uses this property.
		if (instanceCount > 0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean allIndirectInstancesHaveProperty(OntModel ontModel, OntProperty property, OntClass ontClass, Collection<OntClass> ignoreBranches) {
		// Find all classes in ignored branches
		Set<OntClass> ignoreClasses = new HashSet<OntClass>(ignoreBranches);
		ignoreClasses.retainAll(OntologyUtils.listClassDescendants(ontClass));
		for (OntClass ignoreClass: ignoreBranches) {
			ignoreClasses.add(ignoreClass);
			ignoreClasses.addAll(OntologyUtils.listClassDescendants(ignoreClass));
		}

		// Find all non-ignored descendants of this class
		Collection<OntClass> includeClasses = OntologyUtils.listClassDescendants(ontClass);
		includeClasses.removeAll(ignoreClasses);

		// Look for indirect instances that don't have this property, but are not of a class in one of the ignored branches.
		String queryString = OntologyUtils.getSparqlPrefixes(ontModel)
				+ "select (count(?o) as ?count) "
				+ "where { "
				+ "  ?o rdf:type ?t . "
				+ "  unsaid { ?s <" + property.getURI() + "> ?o } . ";
		for (OntClass descendant : ignoreClasses) {
			queryString +=
				  "  unsaid { ?o rdf:type <" + descendant.getURI() + "> } . ";
		}
		queryString +=
				  "  filter ( "
				+ "       ?t = <" + ontClass.getURI() + "> ";
		for (OntClass descendant : includeClasses) {
			queryString +=
				  "    || ?t = <" + descendant.getURI() + "> ";
		}
		queryString +=
				  "  ) . "
				+ "} ";

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

		// If at least one instance was found, then at least one non-ignored indirect instance of this class does not have this property.
		if (instanceCount == 0) {
			return true;
		} else {
			return false;
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