package nl.tudelft.tbm.eeni.owlstructure.utils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

public final class OntologyUtils {
	/**
	 * Closes an iterator if it has interface ClosableIterator. This is useful
	 * to avoid open iterator warnings when we're not interested in all results
	 * of a sparql query.
	 */
	public static void closeIterator(Iterator<?> iterator) {
		while (iterator.hasNext()) {
			iterator.next();
		}
	}

	/**
	 * Check whether a list of candidate classes contains a partial superset of
	 * a given class In inheritance chains where each class extends at most one
	 * other class, this basically checks whether there is an (indirect)
	 * ancestor of the class in the candidates list. When multiple inheritance
	 * is allowed, this is recursively checked for any of the inheritance
	 * branches. Also returns false if a class has no superclass at all.
	 */
	public static boolean containsClassSuperset(OntClass ontClass, Collection<OntClass> candidates) {
		ClosableIterator<OntClass> superClassIterator = ontClass.listSuperClasses();
		while (superClassIterator.hasNext()) {
			OntClass superClass = superClassIterator.next();
			if (candidates.contains(superClass) || containsClassSuperset(superClass, candidates)) {
				// We've found an ancestor
				superClassIterator.close();
				return true;
			}
		}
		// We've found no ancestors
		return false;
	}

	/**
	 * Check whether a list of candidate classes contains a defining superset of
	 * a given class In inheritance chains where each class extends at most one
	 * other class, this basically checks whether there is an (indirect)
	 * ancestor of the class in the candidates list. When multiple inheritance
	 * is allowed, this is recursively checked for all inheritance branches.
	 * Also returns false if a class has no superclass at all.
	 */
	public static boolean containsCompleteClassSuperset(OntClass ontClass, Collection<OntClass> candidates) {
		ClosableIterator<OntClass> superClassIterator = ontClass.listSuperClasses();
		if (!superClassIterator.hasNext()) {
			// There are no ancestors
			return false;
		}
		while (superClassIterator.hasNext()) {
			OntClass superClass = superClassIterator.next();
			if (!candidates.contains(superClass) && !containsCompleteClassSuperset(superClass, candidates)) {
				// We've found an unlisted ancestor
				superClassIterator.close();
				return false;
			}
		}
		// We've found no unlisted ancestors
		return true;
	}

	/**
	 * This convenience function fetches all namespaces that are currently part
	 * of the ontology model and known to Jena and dumps them as a sparql prefix
	 * list
	 */
	public static String getSparqlPrefixes(OntModel ontModel) {
		String sparqlPrefixes = "";

		Map<String, String> prefixMap = ontModel.getNsPrefixMap();
		Iterator<Map.Entry<String, String>> prefixIterator = prefixMap.entrySet().iterator();
		while (prefixIterator.hasNext()) {
			Map.Entry<String, String> prefixPair = prefixIterator.next();
			sparqlPrefixes += "PREFIX " + prefixPair.getKey() + ": <" + prefixPair.getValue() + ">\n";
		}

		return sparqlPrefixes;
	}

	/**
	 * Returns the OntClass instance of owl:Thing
	 */
	public static OntClass getOwlThing(OntModel ontModel) {
		return ontModel.createClass("http://www.w3.org/2002/07/owl#Thing");
	}

	/**
	 * List all (indirect) parents of an ontology class That is, list its
	 * parent, its parent's parent, etc.
	 */
	public static Collection<OntClass> listClassAncestors(OntClass ontClass) {
		Collection<OntClass> ancestors = new ArrayList<OntClass>();
		Iterator<OntClass> superClassIterator = ontClass.listSuperClasses();
		while (superClassIterator.hasNext()) {
			OntClass superClass = superClassIterator.next();
			ancestors.add(superClass);
			ancestors.addAll(listClassAncestors(superClass));
		}
		return ancestors;
	}

	/**
	 * List all (indirect) descendants of an ontology class That is, list its
	 * children, its children's children, etc.
	 */
	public static Collection<OntClass> listClassDescendants(OntClass ontClass) {
		Collection<OntClass> descendants = new ArrayList<OntClass>();
		Iterator<OntClass> subClassIterator = ontClass.listSubClasses();
		while (subClassIterator.hasNext()) {
			OntClass subClass = subClassIterator.next();
			descendants.add(subClass);
			descendants.addAll(listClassDescendants(subClass));
		}
		return descendants;
	}

	public static OntModel createOntology() {
		try {
			return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static OntModel loadOntology(String path) {
		try {
			OntModel ontModel = OntologyUtils.createOntology();
	
			// use the FileManager to find the input file and read it in
			FileManager fm = FileManager.get();
			fm.addLocatorURL();
			fm.readModel(ontModel, path);
	
			return ontModel;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveOntologyRdf(OntModel ontModel, String path) {
		// Open output stream
		try {
			// Create a rdf writer
			RDFWriter writer = ontModel.getWriter("RDF/XML-ABBREV");
			writer.setProperty("showXmlDeclaration","true");
			writer.setProperty("showDoctypeDeclaration","true");
			
			// Create an output stream
			FileOutputStream stream = new FileOutputStream(path);
			
			// Write to the file
			writer.write(ontModel, stream, ontModel.getNsPrefixURI(""));
			
			// Close the out stream
			stream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}