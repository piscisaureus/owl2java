package de.incunabulum.jakuzi.model;

import java.util.HashMap;
import java.util.Map;

public class NamespaceUtils {

	public static Map<String, String> defaultNs2UriMapping;

	static {
		defaultNs2UriMapping = new HashMap<String, String>();
		defaultNs2UriMapping.put("http://www.w3.org/2001/XMLSchema#", "xsd");
		defaultNs2UriMapping.put("http://www.w3.org/2002/07/owl#", "owl");
		defaultNs2UriMapping.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
		defaultNs2UriMapping.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
		defaultNs2UriMapping.put("http://www.topbraidcomposer.org/owl/2006/09/sparql.owl#", "sparql");
		defaultNs2UriMapping.put("http://www.daml.org/2001/03/daml+oil#", "daml");
		// defaultNs2UriMapping.put("http://xmlns.com/foaf/0.1/", "foaf");
	}

}
