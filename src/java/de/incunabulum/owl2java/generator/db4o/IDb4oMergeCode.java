package de.incunabulum.owl2java.generator.db4o;

import com.hp.hpl.jena.ontology.OntModel;

public interface IDb4oMergeCode {
	
    public void run(OntModel ontModel, String dbPath);
    public void run(OntModel ontModel, String dbPath, boolean deleteDbFirst);

    public void run(String ontUri, String dbPath);
    public void run(String ontUri, String dbPath, boolean deleteDbFirst);

    

}
